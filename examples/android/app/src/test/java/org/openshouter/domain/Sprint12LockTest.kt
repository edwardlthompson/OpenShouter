package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeThresholdTest {
    @Test
    fun detectsAboveThresholdAfterCooldown() {
        assertTrue(ShakeThreshold.isShake(3.0, 2.4f, 2_000_000_000L, 0L))
        assertFalse(ShakeThreshold.isShake(2.0, 2.4f, 2_000_000_000L, 0L))
        assertFalse(ShakeThreshold.isShake(3.0, 2.4f, 100L, 0L))
    }

    @Test
    fun restAtGravityIsAboutOneG() {
        assertEquals(1f, ShakeThreshold.gForce(0f, 0f, ShakeThreshold.GRAVITY), 0.01f)
    }
}

class TtsVoiceTest {
    @Test
    fun clampsPitchAndTag() {
        val clamped = TtsVoice(pitch = 9f, languageTag = "x".repeat(40)).clamp()
        assertEquals(TtsVoice.MAX_PITCH, clamped.pitch, 0.01f)
        assertEquals(TtsVoice.MAX_TAG, clamped.languageTag.length)
    }
}

class TtsFormatTokenTest {
    @Test
    fun rendersTickerSubtextAndTime() {
        val spoken = TtsFormat.notification(
            "%app %ticker %subtext %time",
            "Mail",
            "Title",
            "Body",
            mapOf("ticker" to "Tick", "subtext" to "Sub", "time" to "3:00"),
        )
        assertEquals("Mail Tick Sub 3:00", spoken)
    }
}
