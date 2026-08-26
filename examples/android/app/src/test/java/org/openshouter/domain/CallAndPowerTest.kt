package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.call.CallChannel

class IncomingCallTest {
    @Test
    fun loopsOnlyWhileRinging() {
        val ringing = IncomingCallEvent("555", "Ada", CallPhase.RINGING)
        assertTrue(ringing.shouldLoop)
        assertEquals("Ada", ringing.spokenName)
        assertTrue(IncomingCallEvent("555", null, CallPhase.IDLE).shouldStop)
    }

    @Test
    fun blankNumberSpeaksUnknown() {
        assertEquals("an unknown number", IncomingCallEvent("", null, CallPhase.RINGING).spokenName)
    }

    @Test
    fun unknownNumberSpeaksDigits() {
        assertEquals("5 5 5 1 2 1 2 1 2 1", IncomingCallEvent("555-121-2121", null, CallPhase.RINGING).spokenName)
        val spoken = CallChannel.incoming(AppSettings(), "+1 (555) 123-4567", null)
        assertEquals("Incoming call from 5 5 5 1 2 3 4 5 6 7", spoken?.utterance)
    }

    @Test
    fun voipCallSpeaksAppName() {
        val unknown = CallChannel.incoming(AppSettings(), "", null, appLabel = "WhatsApp")
        assertEquals("Incoming call from WhatsApp", unknown?.utterance)
        assertTrue(unknown!!.looping)
        val named = CallChannel.incoming(AppSettings(), "5551212", "Ada", appLabel = "WhatsApp")
        assertEquals("Incoming WhatsApp call from Ada", named?.utterance)
    }
}

class CallNumberResolverTest {
    @Test
    fun prefersHintOverCallLog() {
        assertEquals("555", CallNumberResolver.prefer("555", "999"))
        assertEquals("999", CallNumberResolver.prefer("  ", "999"))
        assertEquals("", CallNumberResolver.prefer("", null))
    }
}

class PowerRulesTest {
    @Test
    fun thresholdsAndPhrases() {
        val settings = AppSettings(batteryFullPercent = 100, batteryLowPercent = 15)
        assertTrue(PowerRules.isFullThreshold(100, settings))
        assertTrue(PowerRules.isLowThreshold(10, settings))
        assertFalse(PowerRules.isLowThreshold(50, settings))
        assertEquals("Power connected.", PowerRules.spoken(PowerEvent(PowerKind.CONNECTED, null)))
    }
}
