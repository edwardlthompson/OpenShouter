package org.openshouter.tts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeakGenerationTest {
    @Test
    fun interruptInvalidatesInFlightSpeak() {
        var gen = 0L
        val started = gen
        assertTrue(SpeakGeneration.isCurrent(gen, started))
        gen = SpeakGeneration.bump(gen)
        assertEquals(1L, gen)
        assertFalse(SpeakGeneration.isCurrent(gen, started))
        assertTrue(SpeakGeneration.isCurrent(gen, gen))
    }
}
