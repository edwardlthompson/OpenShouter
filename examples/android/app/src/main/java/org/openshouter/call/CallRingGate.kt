package org.openshouter.call

import org.openshouter.domain.CallPhase

/** Pure helpers for cellular RINGING race vs OFFHOOK interrupt. */
object CallRingGate {
    fun shouldContinueRingWork(phase: CallPhase, generation: Long, startedGeneration: Long): Boolean =
        phase == CallPhase.RINGING && generation == startedGeneration

    fun nextGeneration(current: Long): Long = current + 1L
}
