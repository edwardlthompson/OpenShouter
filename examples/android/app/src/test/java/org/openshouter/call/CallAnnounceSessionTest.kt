package org.openshouter.call

import org.junit.Assert.assertEquals
import org.junit.Test

class CallAnnounceSessionTest {
    @Test
    fun sameKeyIgnoredUntilCleared() {
        val session = CallAnnounceSession()
        assertEquals(
            CallAnnounceAction.ANNOUNCE,
            session.decide("com.whatsapp", "k1", VoipCallPhase.INCOMING),
        )
        assertEquals(
            CallAnnounceAction.IGNORE,
            session.decide("com.whatsapp", "k1", VoipCallPhase.INCOMING),
        )
        assertEquals(
            CallAnnounceAction.INTERRUPT,
            session.decide("com.whatsapp", "k1", VoipCallPhase.IN_CALL),
        )
        assertEquals(
            CallAnnounceAction.ANNOUNCE,
            session.decide("com.whatsapp", "k2", VoipCallPhase.INCOMING),
        )
    }

    @Test
    fun removedClearsSoNewKeyAnnounces() {
        val session = CallAnnounceSession()
        session.decide("com.whatsapp", "k1", VoipCallPhase.INCOMING)
        assertEquals(CallAnnounceAction.INTERRUPT, session.onRemoved("com.whatsapp", "k1"))
        assertEquals(CallAnnounceAction.IGNORE, session.onRemoved("com.whatsapp", "k1"))
        assertEquals(
            CallAnnounceAction.ANNOUNCE,
            session.decide("com.whatsapp", "k2", VoipCallPhase.INCOMING),
        )
    }

    @Test
    fun blankKeyFallsBackToPackage() {
        val session = CallAnnounceSession()
        assertEquals("com.whatsapp", session.key("com.whatsapp", "  "))
        assertEquals(
            CallAnnounceAction.ANNOUNCE,
            session.decide("com.whatsapp", "", VoipCallPhase.INCOMING),
        )
        assertEquals(
            CallAnnounceAction.IGNORE,
            session.decide("com.whatsapp", "  ", VoipCallPhase.INCOMING),
        )
        assertEquals(
            CallAnnounceAction.INTERRUPT,
            session.onRemoved("com.whatsapp", ""),
        )
    }

    @Test
    fun endedClearsSession() {
        val session = CallAnnounceSession()
        session.decide("org.telegram.messenger", "a", VoipCallPhase.INCOMING)
        assertEquals(
            CallAnnounceAction.INTERRUPT,
            session.decide("org.telegram.messenger", "a", VoipCallPhase.ENDED),
        )
        assertEquals(
            CallAnnounceAction.ANNOUNCE,
            session.decide("org.telegram.messenger", "b", VoipCallPhase.INCOMING),
        )
    }
}
