package org.openshouter.notification

import org.openshouter.call.CallAnnounceAction
import org.openshouter.call.CallAnnounceSession
import org.openshouter.call.CallDedup
import org.openshouter.call.CallLoopGate
import org.openshouter.call.CallPosted
import org.openshouter.domain.AppSettings
import org.openshouter.domain.RegexAction
import org.openshouter.domain.RegexRule
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.service.OpenShouterEntryPoint

internal object NotificationPostedVoip {
    suspend fun handle(
        facts: NotificationFacts,
        ep: OpenShouterEntryPoint,
        settings: AppSettings,
        label: String,
        session: CallAnnounceSession,
    ) {
        val action = CallPosted.action(
            facts.app, facts.notificationKey, facts.categoryCall, facts.isOngoing, facts.callType, session,
        )
        if (action == CallAnnounceAction.INTERRUPT) {
            CallLoopGate.clear()
            ep.tts().interrupt()
            return
        }
        if (action != CallAnnounceAction.ANNOUNCE) return
        val dedupKey = facts.title.ifBlank { facts.people }.ifBlank { facts.app }
        if (!CallDedup.shouldAnnounce(dedupKey)) return
        val incoming = CallPosted.eventFor(settings, facts.title, facts.people, label, facts.app) ?: return
        CallLoopGate.onVoipAnnounce(facts.app)
        NotificationHistory.speakOrIgnore(
            ep, settings, ShoutChannel.CALL, SpokenEvent.Kind.CALL, incoming.utterance, facts,
            looping = incoming.looping, repeatCount = incoming.repeatCount,
        )
    }

    suspend fun regexRules(ep: OpenShouterEntryPoint): List<RegexRule> =
        ep.regex().snapshot().map {
            RegexRule(
                it.pattern,
                runCatching { RegexAction.valueOf(it.action) }.getOrDefault(RegexAction.IGNORE),
                it.replacement,
            )
        }
}
