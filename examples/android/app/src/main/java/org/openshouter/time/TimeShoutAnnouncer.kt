package org.openshouter.time

import android.content.Context
import android.text.format.DateFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.data.HistoryDao
import org.openshouter.data.SettingsRepository
import org.openshouter.data.ShoutHistoryStore
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TimeShout
import org.openshouter.domain.TtsFormat
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

@Singleton
class TimeShoutAnnouncer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val tts: TtsController,
    private val gate: SpeakGate,
    private val history: HistoryDao,
) {
    @Volatile private var lastSlot: Long = Long.MIN_VALUE

    suspend fun announce(requireAligned: Boolean) {
        val snap = settings.snapshot()
        if (!snap.announcerEnabled || !snap.timeShoutEnabled) return
        if (!gate.allow(snap, ShoutChannel.TIME)) return
        val now = System.currentTimeMillis()
        val interval = snap.timeShoutIntervalMinutes
        val slot = TimeShout.currentSlotStartMillis(now, interval)
        if (!TimeShout.shouldSpeakSlot(slot, lastSlot, now, requireAligned, interval)) return
        lastSlot = slot
        val zoned = Instant.ofEpochMilli(slot).atZone(ZoneId.systemDefault())
        val clock = TimeShout.formatClockForSpeech(
            zoned,
            snap.timeHourStyle,
            DateFormat.is24HourFormat(context),
        )
        val phrase = TtsFormat.time(snap.timeFormat, clock)
        if (phrase.isNotBlank()) {
            ShoutHistoryStore.insertOnce(history, SpokenEvent.Kind.TIME, phrase)
            tts.speak(ChannelStates.spoken(snap, ShoutChannel.TIME, SpokenEvent.Kind.TIME, phrase))
        }
    }
}
