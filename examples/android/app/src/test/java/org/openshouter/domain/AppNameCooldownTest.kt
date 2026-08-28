package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNameCooldownTest {
    @Test
    fun offAlwaysAllows() {
        assertTrue(AppNameCooldown.allow(1_000L, 1_001L, 0))
        assertTrue(AppNameCooldown.allow(0L, 5_000L, 30))
    }

    @Test
    fun sameAppWithinWindowIsSuppressed() {
        assertFalse(AppNameCooldown.allow(1_000L, 10_000L, 30))
        assertTrue(AppNameCooldown.allow(1_000L, 31_000L, 30))
    }

    @Test
    fun clampSnapsToDropdownOptions() {
        assertEquals(0, AppNameCooldown.clampSeconds(-4))
        assertEquals(30, AppNameCooldown.clampSeconds(30))
        assertEquals(60, AppNameCooldown.clampSeconds(50))
        assertEquals(300, AppNameCooldown.clampSeconds(400))
    }

    @Test
    fun defaultThirtySecondsOnUnsetChannel() {
        val settings = AppSettings()
        assertEquals(30, AppNameCooldown.secondsFor(settings, ShoutChannel.MESSAGE))
        assertEquals(30, AppNameCooldown.secondsFor(settings, ShoutChannel.NOTIFICATION))
        val off = AppSettings(
            channelStates = mapOf(
                ShoutChannel.MESSAGE to ChannelDeviceState(appNameCooldownSeconds = 0),
            ),
        )
        assertEquals(0, AppNameCooldown.secondsFor(off, ShoutChannel.MESSAGE))
        assertTrue(AppNameCooldown.include(off, ShoutChannel.MESSAGE, 1_000L, 1_100L))
    }

    @Test
    fun appLabelMatchIsCaseInsensitive() {
        assertTrue(AppNameCooldown.isAppLabel("Messages", "messages"))
        assertFalse(AppNameCooldown.isAppLabel("Jane", "Messages"))
        assertFalse(AppNameCooldown.isAppLabel("", "Messages"))
    }
}
