package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
