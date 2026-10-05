package org.openshouter.call

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.domain.CallPhase

class CallRingGateTest {
    @Test
    fun continueOnlyWhileRingingAndSameGeneration() {
        assertTrue(CallRingGate.shouldContinueRingWork(CallPhase.RINGING, 1L, 1L))
        assertFalse(CallRingGate.shouldContinueRingWork(CallPhase.OFFHOOK, 1L, 1L))
        assertFalse(CallRingGate.shouldContinueRingWork(CallPhase.IDLE, 1L, 1L))
        assertFalse(CallRingGate.shouldContinueRingWork(CallPhase.RINGING, 2L, 1L))
    }

    @Test
    fun offhookBumpsGenerationSoStaleTwinAborts() {
        var gen = 0L
        val started = gen
        assertTrue(CallRingGate.shouldContinueRingWork(CallPhase.RINGING, gen, started))
        gen = CallRingGate.nextGeneration(gen)
        assertEquals(1L, gen)
        assertFalse(CallRingGate.shouldContinueRingWork(CallPhase.OFFHOOK, gen, started))
        assertFalse(CallRingGate.shouldContinueRingWork(CallPhase.RINGING, gen, started))
    }
}
