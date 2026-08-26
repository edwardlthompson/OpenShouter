package org.openshouter.notification

import org.openshouter.call.CallNotification
import org.openshouter.domain.AppOverride
import org.openshouter.domain.AppSpeakPolicy
import org.openshouter.domain.IgnoreReason
import org.openshouter.domain.NotificationPolicy
import org.openshouter.domain.NotificationRank
import org.openshouter.domain.RegexAction
import org.openshouter.domain.RegexFilter
import org.openshouter.domain.RegexRule
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.message.MessageChannel
import org.openshouter.service.OpenShouterEntryPoint

internal object NotificationPosted {
    suspend fun handle(
        facts: NotificationFacts,
        ep: OpenShouterEntryPoint,
        clock: RepeatClock,
        label: String,
        priorityDnd: Boolean,
    ) {
        val settings = ep.settings().snapshot()
        if (CallNotification.routeAsCall(
                facts.app,
                facts.categoryCall,
                facts.isOngoing,
                settings.callsEnabled,
            )
        ) {
            val incoming = CallNotification.event(settings, facts.title, facts.people, label)
                ?: return
            NotificationHistory.speakOrIgnore(
                ep, settings, ShoutChannel.CALL, SpokenEvent.Kind.CALL, incoming.utterance, facts,
                silentExempt = true, looping = incoming.looping,
            )
            return
        }
        if (!settings.notificationsEnabled) return
        if (MessageChannel.isMessaging(facts.app)) {
            val spoken = MessageChannel.utterance(
                settings,
                MessageChannel.parse(facts.title, facts.text, facts.people),
                null,
            ) ?: return
            NotificationHistory.speakOrIgnore(
                ep, settings, ShoutChannel.MESSAGE, SpokenEvent.Kind.MESSAGE, spoken, facts,
            )
            return
        }
        val rule = if (facts.isTest) {
            org.openshouter.domain.AppSpeakRule(facts.app, true, true)
        } else {
            AppSpeakPolicy.ruleFor(facts.app, ep.appSpeak().snapshot())
        } ?: return
        val now = System.currentTimeMillis()
        val key = NotificationPolicy.repeatKey(facts.app, facts.title, facts.text)
        val reason = settings.notificationPolicy.decide(
            facts.title, facts.text, facts.isGroup, key, clock.lastKey, clock.lastAt, now,
        )
        if (reason != IgnoreReason.NONE) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, key, now, reason)
            return
        }
        if (!NotificationRank.allows(settings.notificationPolicy.minImportance, facts.rank)) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, key, now, IgnoreReason.IMPORTANCE)
            return
        }
        val format = AppOverride(facts.app, settings.appFormats[facts.app]).mergeFormat(settings.ttsFormat)
        val spoken = NotificationUtterance.build(
            rule, facts.app, settings.messageChannel.speakBody, format, label,
            facts.title, facts.text, facts.tokens,
        )
        if (spoken.isBlank()) return
        val filtered = RegexFilter.apply(spoken, regexRules(ep))
        if (filtered == null) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, key, now, IgnoreReason.FILTER)
            return
        }
        clock.lastKey = key
        clock.lastAt = now
        val highOrCall = facts.categoryCall || facts.rank >= NotificationRank.HIGH
        NotificationHistory.speakOrIgnore(
            ep, settings, ShoutChannel.NOTIFICATION, SpokenEvent.Kind.NOTIFICATION, filtered, facts,
            settings.notificationPolicy.dndExempt(priorityDnd, highOrCall),
        )
    }

    private suspend fun regexRules(ep: OpenShouterEntryPoint): List<RegexRule> =
        ep.regex().snapshot().map {
            RegexRule(
                it.pattern,
                runCatching { RegexAction.valueOf(it.action) }.getOrDefault(RegexAction.IGNORE),
                it.replacement,
            )
        }
}
