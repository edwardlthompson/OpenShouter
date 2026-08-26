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
        assertEquals(TtsStream.NOTIFICATION, TtsStream.NOTIFICATION.playable(thisMuted = true, mediaMuted = true))
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

    @Test
    fun carModeMapsNotificationToMedia() {
        assertEquals(TtsStream.MEDIA, TtsStream.NOTIFICATION.forCarPlayback(true))
        assertEquals(TtsStream.NOTIFICATION, TtsStream.NOTIFICATION.forCarPlayback(false))
        assertEquals(TtsStream.MEDIA, TtsStream.MEDIA.forCarPlayback(true))
        assertEquals(TtsStream.ALARM, TtsStream.ALARM.forCarPlayback(true))
    }

    @Test
    fun carOutputsIncludeA2dpUsbAndBus() {
        assertTrue(CarAudioRoute.isCarOutput(CarAudioRoute.TYPE_A2DP))
        assertTrue(CarAudioRoute.isCarOutput(CarAudioRoute.TYPE_USB_ACCESSORY))
        assertTrue(CarAudioRoute.isCarOutput(CarAudioRoute.TYPE_BUS))
        assertFalse(CarAudioRoute.isCarOutput(2))
    }
}

class TtsVoiceTest {
    @Test
    fun clampsPitchAndTag() {
        val clamped = TtsVoice(pitch = 9f, languageTag = "x".repeat(40)).clamp()
        assertEquals(TtsVoice.MAX_PITCH, clamped.pitch, 0.01f)
        assertEquals(TtsVoice.MAX_TAG, clamped.languageTag.length)
    }

    @Test
    fun clampsEngineNameAndQuality() {
        val clamped = TtsVoice(
            engine = "com.bad engine/id",
            voiceName = "en-us-x-sfg-local!!!",
            minQuality = 900,
        ).clamp()
        assertEquals("com.badengineid", clamped.engine)
        assertEquals("en-us-x-sfg-local", clamped.voiceName)
        assertEquals(TtsVoice.QUALITY_VERY_HIGH, clamped.minQuality)
    }
}

class TtsLangCatalogTest {
    @Test
    fun listsRhvoiceAndSherpaAndPrefersSherpaForHighQuality() {
        assertTrue(TtsLangCatalog.covers(TtsLangCatalog.sherpaTags(), "ja-JP").not())
        assertTrue(TtsLangCatalog.covers(TtsLangCatalog.sherpaTags(), "ru-RU"))
        assertTrue(TtsLangCatalog.covers(TtsLangCatalog.rhvoiceTags(), "ru"))
        assertEquals(
            listOf(TtsSourceCatalog.SHERPA, TtsSourceCatalog.RHVOICE),
            TtsLangCatalog.enginesFor("ru-RU"),
        )
        assertEquals(
            TtsSourceCatalog.SHERPA,
            TtsLangCatalog.keepOrPrefer("", "ru-RU", emptyList(), setOf(TtsSourceCatalog.SHERPA), TtsVoice.QUALITY_VERY_HIGH),
        )
        assertTrue(TtsLangCatalog.merge(listOf("ja-JP")).contains("ja-JP"))
        assertTrue(TtsLangCatalog.merge(listOf("ja-JP")).contains("en-US"))
        val high = TtsVoiceCandidate("ja", "ja-JP", TtsVoice.QUALITY_VERY_HIGH, 40, false)
        val filtered = TtsLangCatalog.filterTags(
            TtsLangCatalog.merge(listOf("ja-JP")),
            listOf(high),
            TtsVoice.QUALITY_VERY_HIGH,
        )
        assertTrue(filtered.contains("en-US"))
        assertTrue(filtered.contains("ja-JP"))
        assertTrue(filtered.none { it.startsWith("ru") })
        assertFalse(TtsLangCatalog.meets("ru-RU", emptyList(), TtsVoice.QUALITY_VERY_HIGH))
        assertEquals(TtsVoice.QUALITY_VERY_HIGH, TtsLangCatalog.catalogQuality("en-GB"))
        assertEquals(TtsVoice.QUALITY_HIGH, TtsLangCatalog.catalogQuality("ru-RU"))
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
