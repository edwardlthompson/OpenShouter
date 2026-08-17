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

class TtsStreamPlayableTest {
    @Test
    fun mutedNotificationFallsBackToMedia() {
        assertEquals(TtsStream.MEDIA, TtsStream.NOTIFICATION.playable(thisMuted = true, mediaMuted = false))
        assertEquals(TtsStream.NOTIFICATION, TtsStream.NOTIFICATION.playable(thisMuted = false, mediaMuted = true))
        assertEquals(TtsStream.ALARM, TtsStream.NOTIFICATION.playable(thisMuted = true, mediaMuted = true))
        assertEquals(
            TtsStream.NOTIFICATION,
            TtsStream.NOTIFICATION.playable(thisMuted = true, mediaMuted = true, alarmMuted = true),
        )
        assertEquals(TtsStream.ALARM, TtsStream.ALARM.playable(thisMuted = false, mediaMuted = false))
        assertEquals(
            TtsStream.NOTIFICATION,
            TtsStream.NOTIFICATION.playable(
                thisMuted = true,
                mediaMuted = false,
                ringerSilent = true,
                allowSilentVibrate = false,
            ),
        )
        assertEquals(
            TtsStream.MEDIA,
            TtsStream.NOTIFICATION.playable(
                thisMuted = true,
                mediaMuted = false,
                ringerSilent = true,
                allowSilentVibrate = true,
            ),
        )
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
            "%app %ticker %subtext %info %bigtitle %bigsummary %lines %time",
            "Mail",
            "Title",
            "Body",
            mapOf(
                "ticker" to "Tick",
                "subtext" to "Sub",
                "info" to "Info",
                "bigtitle" to "Big",
                "bigsummary" to "Sum",
                "lines" to "L1 L2",
                "time" to "3:00",
            ),
        )
        assertEquals("Mail Tick Sub Info Big Sum L1 L2 3:00", spoken)
    }
}
