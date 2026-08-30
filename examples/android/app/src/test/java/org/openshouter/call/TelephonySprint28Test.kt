package org.openshouter.call

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.domain.TelephonyExtras

class TelephonySprint28Test {

    @Test
    fun callDedupSuppressesDuplicateWithinWindow() {
        CallDedup.clear()
        assertTrue(CallDedup.shouldAnnounce("Alice", 1000L))
        assertFalse(CallDedup.shouldAnnounce("Alice", 2000L))
        assertTrue(CallDedup.shouldAnnounce("Bob", 2500L))
        assertTrue(CallDedup.shouldAnnounce("Alice", 8000L))
    }

    @Test
    fun telephonyExtrasFormatDuration() {
        assertEquals("0 seconds", TelephonyExtras.formatDuration(0))
        assertEquals("45 seconds", TelephonyExtras.formatDuration(45))
        assertEquals("1 minute", TelephonyExtras.formatDuration(60))
        assertEquals("2 minutes 15 seconds", TelephonyExtras.formatDuration(135))
        assertEquals("1 minute 1 second", TelephonyExtras.formatDuration(61))
    }

    @Test
    fun telephonyExtrasConferenceHint() {
        assertEquals("Conference call from Alice with 3 participants", TelephonyExtras.conferenceHint(3, "Alice"))
        assertEquals("Conference call with 4 participants", TelephonyExtras.conferenceHint(4))
        assertEquals("Conference call from Alice", TelephonyExtras.conferenceHint(1, "Alice"))
        assertEquals("Conference call", TelephonyExtras.conferenceHint(0))
    }

    @Test
    fun telephonyExtrasCallWaiting() {
        assertEquals("Call waiting from Alice", TelephonyExtras.callWaiting("Alice"))
        assertEquals("Call waiting from Unknown", TelephonyExtras.callWaiting(""))
    }
}
