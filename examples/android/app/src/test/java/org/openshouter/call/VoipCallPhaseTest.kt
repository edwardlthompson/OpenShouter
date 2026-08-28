package org.openshouter.call

import org.junit.Assert.assertEquals
import org.junit.Test

class VoipCallPhaseTest {
    @Test
    fun incomingVsInCallFromCallStyle() {
        assertEquals(
            VoipCallPhase.INCOMING,
            VoipCallPhaseLogic.phase(categoryCall = true, isOngoing = true, callType = VoipCallPhaseLogic.TYPE_INCOMING),
        )
        assertEquals(
            VoipCallPhase.IN_CALL,
            VoipCallPhaseLogic.phase(categoryCall = true, isOngoing = true, callType = VoipCallPhaseLogic.TYPE_ONGOING),
        )
        assertEquals(
            VoipCallPhase.INCOMING,
            VoipCallPhaseLogic.phase(categoryCall = true, isOngoing = true, callType = VoipCallPhaseLogic.TYPE_SCREENING),
        )
    }

    @Test
    fun missingCallStyleFirstPostIsIncoming() {
        assertEquals(
            VoipCallPhase.INCOMING,
            VoipCallPhaseLogic.phase(categoryCall = true, isOngoing = true, callType = null),
        )
        assertEquals(
            VoipCallPhase.INCOMING,
            VoipCallPhaseLogic.phase(categoryCall = false, isOngoing = true, callType = null),
        )
        assertEquals(
            VoipCallPhase.ENDED,
            VoipCallPhaseLogic.phase(categoryCall = false, isOngoing = false, callType = null),
        )
    }
}
