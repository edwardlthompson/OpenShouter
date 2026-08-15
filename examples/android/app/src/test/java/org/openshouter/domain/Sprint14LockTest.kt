package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderIntervalTest {
    @Test
    fun snapsToNearestCalendarInterval() {
        assertEquals(ReminderInterval.HOUR, ReminderInterval.normalize(70))
        assertEquals(ReminderInterval.DAY, ReminderInterval.normalize(20 * 60))
        assertEquals(ReminderInterval.WEEK, ReminderInterval.normalize(8 * 24 * 60))
        val now = 1_000_000L
        assertEquals(now + ReminderInterval.DAY * 60_000L, ReminderInterval.nextAt(now, ReminderInterval.DAY))
    }
}

class ChannelSpokenTest {
    @Test
    fun spokenCopiesChannelStreamAndRepeat() {
        val settings = AppSettings(
            ttsPlayback = TtsPlaybackPolicy(stream = TtsStream.NOTIFICATION, repeatCount = 0),
            channelStates = mapOf(
                ShoutChannel.CALL to ChannelDeviceState(
                    stream = TtsStream.ALARM,
                    repeatCount = 2,
                ),
            ),
        )
        val event = ChannelStates.spoken(settings, ShoutChannel.CALL, SpokenEvent.Kind.CALL, "x")
        assertEquals(TtsStream.ALARM, event.stream)
        assertEquals(2, event.repeatCount)
    }
}
