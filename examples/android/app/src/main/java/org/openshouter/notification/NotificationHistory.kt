package org.openshouter.notification

import org.openshouter.data.HistoryEntity
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.IgnoreReason
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.service.OpenShouterEntryPoint

internal object NotificationHistory {
    suspend fun recordIgnore(
        ep: OpenShouterEntryPoint,
        settings: AppSettings,
        clock: RepeatClock,
        facts: NotificationFacts,
        key: String,
        now: Long,
        reason: IgnoreReason,
    ) {
        if (!settings.notificationPolicy.recordIgnore(
                reason, key, clock.lastRecordedKey, clock.lastRecordedAt, now,
            )
        ) {
            return
        }
        clock.lastRecordedKey = key
        clock.lastRecordedAt = now
        if (reason == IgnoreReason.REPEAT) {
            clock.lastKey = key
            clock.lastAt = now
        }
        insert(ep, facts, "", reason)
    }

    suspend fun speakOrIgnore(
        ep: OpenShouterEntryPoint,
        settings: AppSettings,
        channel: ShoutChannel,
        kind: SpokenEvent.Kind,
        spoken: String,
        facts: NotificationFacts,
        silentExempt: Boolean = false,
    ) {
        val deny = ep.gate().denyReason(settings, channel, silentExempt)
        if (deny != null) {
            insert(ep, facts, "", deny)
            return
        }
        insert(ep, facts, spoken, IgnoreReason.NONE)
        ep.tts().speak(ChannelStates.spoken(settings, channel, kind, spoken))
    }

    private suspend fun insert(
        ep: OpenShouterEntryPoint,
        facts: NotificationFacts,
        spoken: String,
        reason: IgnoreReason,
    ) {
        ep.history().insert(
            HistoryEntity(
                postedAt = facts.postedAt,
                packageName = facts.app,
                title = facts.title,
                text = facts.text,
                spoken = spoken,
                ignoreReason = reason.name,
            ),
        )
        ep.history().pruneTo(100)
    }
}
