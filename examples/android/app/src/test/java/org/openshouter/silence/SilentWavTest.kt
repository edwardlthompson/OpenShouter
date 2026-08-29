package org.openshouter.silence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SilentWavTest {
    @Test
    fun bytesAreAValidSilentWav() {
        val wav = SilentWav.bytes()
        assertTrue(wav.size > 44)
        assertEquals("RIFF", wav.decodeToString(0, 4))
        assertEquals("WAVE", wav.decodeToString(8, 12))
        assertEquals("fmt ", wav.decodeToString(12, 16))
        assertEquals("data", wav.decodeToString(36, 40))
        assertTrue(wav.drop(44).all { it == 0.toByte() })
    }

    @Test
    fun silentUriRecognizesOurPack() {
        assertTrue(SilentWav.isSilentUri(""))
        assertTrue(SilentWav.isSilentUri("null"))
        assertTrue(SilentWav.isSilentUri("content://media/1/OpenShouter Silent.wav"))
        assertFalse(SilentWav.isSilentUri("content://media/external/audio/media/9"))
    }
}
