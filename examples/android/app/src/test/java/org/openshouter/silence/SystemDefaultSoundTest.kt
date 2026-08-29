package org.openshouter.silence

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemDefaultSoundTest {
    @Test
    fun noneAndOurPackAreSilentExceptUnreliableOemNone() {
        assertTrue(SystemDefaultSound.isSilent(null, oemUnreliableNone = false))
        assertTrue(SystemDefaultSound.isSilent("", oemUnreliableNone = false))
        assertFalse(SystemDefaultSound.isSilent(null, oemUnreliableNone = true))
        assertFalse(SystemDefaultSound.isSilent("", oemUnreliableNone = true))
        assertTrue(SystemDefaultSound.isSilent("content://media/1/OpenShouter Silent.wav", true))
        assertFalse(SystemDefaultSound.isSilent("content://media/external/audio/media/9", false))
    }
}