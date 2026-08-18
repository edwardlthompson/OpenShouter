package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsPlaybackPolicyTest {
    @Test
    fun clampsOutOfRange() {
        val clamped = TtsPlaybackPolicy(delaySeconds = 99, maxLength = 9_000, repeatMinutes = 200).clamp()
        assertEquals(TtsPlaybackPolicy.MAX_DELAY, clamped.delaySeconds)
        assertEquals(TtsPlaybackPolicy.MAX_CHARS, clamped.maxLength)
        assertEquals(TtsPlaybackPolicy.MAX_REPEAT, clamped.repeatMinutes)
    }

    @Test
    fun clipsToMaxLength() {
        val policy = TtsPlaybackPolicy(maxLength = 5)
        assertEquals("hello", policy.prepareUtterance("hello world"))
    }

    @Test
    fun stripsEmojisWhenDisabled() {
        val policy = TtsPlaybackPolicy(speakEmojis = false)
        assertEquals("hi there", policy.prepareUtterance("hi 🎉 there"))
    }

    @Test
    fun blankAfterStripIsEmpty() {
        val policy = TtsPlaybackPolicy(speakEmojis = false)
        assertEquals("", policy.prepareUtterance("🎉"))
    }
}

class DeviceStatePolicyTest {
    @Test
    fun defaultAllowsIdlePhone() {
        assertTrue(DeviceStatePolicy().allows(true, false, false, false))
        assertFalse(DeviceStatePolicy().allows(true, false, silentOrVibrate = true, inCall = false))
    }

    @Test
    fun defaultBlocksInCall() {
        assertFalse(DeviceStatePolicy().allows(true, false, false, inCall = true))
    }

    @Test
    fun silentFlagHonored() {
        val policy = DeviceStatePolicy(allowSilentVibrate = false)
        assertFalse(policy.allows(true, false, silentOrVibrate = true, inCall = false))
        assertTrue(policy.allows(true, false, silentOrVibrate = false, inCall = false))
    }

    @Test
    fun screenOffOnlyViaFlags() {
        val policy = DeviceStatePolicy(allowScreenOn = false, allowScreenOff = true)
        assertFalse(policy.allows(screenOn = true, headsetOn = false, false, false))
        assertTrue(policy.allows(screenOn = false, headsetOn = false, false, false))
    }
}

class AnnouncementGateDeviceStateTest {
    @Test
    fun deviceStateBlocksInCall() {
        assertFalse(
            AnnouncementGate.allow(
                AppSettings(),
                12 * 60,
                2,
                screenOn = true,
                headsetConnected = false,
                insideSilentGeofence = false,
                inCall = true,
            ),
        )
    }
}
