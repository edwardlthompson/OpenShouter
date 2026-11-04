package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Portfolio: per-app mute + global quiet hours both deny speak. */
class QuietHoursMuteTest {
    @Test
    fun quietHoursDenyWhileEnabled() {
        val settings = AppSettings(
            quietHoursEnabled = true,
            quietStartMinutes = 22 * 60,
            quietEndMinutes = 7 * 60,
            quietDays = QuietHours.ALL_DAYS,
        )
        assertEquals(
            IgnoreReason.GATE_QUIET,
            AnnouncementGate.denyReason(
                settings,
                minuteOfDay = 23 * 60,
                dayOfWeek = 2,
                screenOn = false,
                headsetConnected = true,
                insideSilentGeofence = false,
            ),
        )
        assertTrue(AnnouncementGate.inQuietWindow(23 * 60, 22 * 60, 7 * 60))
        assertFalse(AnnouncementGate.inQuietWindow(12 * 60, 22 * 60, 7 * 60))
    }

    @Test
    fun mutedAppIsDistinctFromUnlisted() {
        val muted = AppSpeakRule("com.spam", false, false)
        assertTrue(AppSpeakPolicy.isMuted(muted))
        assertFalse(AppSpeakPolicy.isMuted(null))
        assertFalse(AppSpeakPolicy.allowsSpeak(muted))
    }
}
