package org.openshouter.notification

import org.openshouter.call.CallAnnounceSession
import org.openshouter.call.CallNotification
import org.openshouter.domain.AppNameCooldown
import org.openshouter.domain.AppOverride
import org.openshouter.domain.AppSpeakPolicy
import org.openshouter.domain.AppSpeakRule
import org.openshouter.domain.IgnoreReason
import org.openshouter.domain.NotificationPolicy
import org.openshouter.domain.NotificationRank
import org.openshouter.domain.RegexFilter
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
        session: CallAnnounceSession,
    ) {
        val settings = ep.settings().snapshot()
        if (CallNotification.routeAsCall(
                facts.app, facts.categoryCall, facts.isOngoing, settings.callsEnabled,
            )
        ) {
            NotificationPostedVoip.handle(facts, ep, settings, label, session)
            return
        }
        if (!settings.notificationsEnabled) return
        if (MessageChannel.isMessaging(facts.app)) {
            handleMessage(facts, ep, settings, clock, label)
            return
        }
        val rule = if (facts.isTest) {
            AppSpeakRule(facts.app, true, true)
        } else {
            AppSpeakPolicy.ruleFor(facts.app, ep.appSpeak().snapshot())
        } ?: return
        val now = System.currentTimeMillis()
        val key = NotificationPolicy.repeatKey(facts.app, facts.title, facts.text)
        if (AppSpeakPolicy.isMuted(rule)) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, key, now, IgnoreReason.APP_MUTED)
            return
        }
        val reason = settings.notificationPolicy.decide(
            facts.title, facts.text, facts.isGroup, key, clock.lastKey, clock.lastAt, now,
        )
        if (reason != IgnoreReason.NONE) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, key, now, reason)
            return
        }
        val ignoreKey = facts.notificationKey
        if (settings.notificationPolicy.ignoreBubbles && facts.isBubble) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, ignoreKey, now, IgnoreReason.BUBBLE)
            return
        }
        if (settings.notificationPolicy.ignoreWorkProfile && facts.isWorkProfile) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, ignoreKey, now, IgnoreReason.WORK_PROFILE)
            return
        }
        val appOverride = settings.appOverrides[facts.app] ?: AppOverride(facts.app, settings.appFormats[facts.app])
        val effectiveImportance = appOverride.minImportance ?: settings.notificationPolicy.minImportance
        if (!NotificationRank.allows(effectiveImportance, facts.rank)) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, key, now, IgnoreReason.IMPORTANCE)
            return
        }
        val format = appOverride.mergeFormat(settings.ttsFormat)
        val appCooldownSec = appOverride.appNameCooldownSeconds
            ?: AppNameCooldown.secondsFor(settings, ShoutChannel.NOTIFICATION)
        val includeApp = AppNameCooldown.allow(clock.appNameAt(facts.app), now, appCooldownSec)
        val spoken = NotificationUtterance.build(
            rule, facts.app, settings.messageChannel.speakBody, format, label,
            facts.title, facts.text, facts.tokens, includeApp,
        )
        if (spoken.isBlank()) return
        val filtered = RegexFilter.apply(spoken, NotificationPostedVoip.regexRules(ep))
        if (filtered == null) {
            NotificationHistory.recordIgnore(ep, settings, clock, facts, key, now, IgnoreReason.FILTER)
            return
        }
        if (includeApp) clock.markAppName(facts.app, now)
        clock.lastKey = key
        clock.lastAt = now
        val highOrCall = facts.categoryCall || facts.rank >= NotificationRank.HIGH
        NotificationHistory.speakOrIgnore(
            ep, settings, ShoutChannel.NOTIFICATION, SpokenEvent.Kind.NOTIFICATION, filtered, facts,
            settings.notificationPolicy.dndExempt(priorityDnd, highOrCall),
        )
    }

    private suspend fun handleMessage(
        facts: NotificationFacts,
        ep: OpenShouterEntryPoint,
        settings: org.openshouter.domain.AppSettings,
        clock: RepeatClock,
        label: String,
    ) {
        val now = System.currentTimeMillis()
        val contactKey = "${facts.app}:${facts.title.trim()}"
        if (settings.notificationPolicy.contactCooldownSeconds > 0 &&
            !AppNameCooldown.allow(
                clock.contactAt(contactKey), now, settings.notificationPolicy.contactCooldownSeconds,
            )
        ) {
            NotificationHistory.recordIgnore(
                ep, settings, clock, facts, contactKey, now, IgnoreReason.CONTACT_COOLDOWN,
            )
            return
        }
        val includeApp = AppNameCooldown.include(
            settings, ShoutChannel.MESSAGE, clock.appNameAt(facts.app), now,
        )
        val spoken = MessageChannel.utterance(
            settings,
            MessageChannel.parse(facts.title, facts.text, facts.people),
            null,
            label,
            includeApp,
        ) ?: return
        if (includeApp) clock.markAppName(facts.app, now)
        clock.markContact(contactKey, now)
        NotificationHistory.speakOrIgnore(
            ep, settings, ShoutChannel.MESSAGE, SpokenEvent.Kind.MESSAGE, spoken, facts,
        )
    }
}
