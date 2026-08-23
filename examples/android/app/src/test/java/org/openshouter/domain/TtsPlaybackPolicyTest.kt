package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsPlaybackPolicyTest {
    @Test
    fun clampsOutOfRange() {
        val clamped = TtsPlaybackPolicy(delaySeconds = 99, maxLength = 9_000, repeatMinutes = 200).clamp()
        assertEquals(TtsPlaybackPolicy.MAX_DELAY, clamped.delaySeconds)
        assertEquals(TtsPlaybackPolicy.MAX_CHARS, clamped.maxLength)
        assertEquals(TtsPlaybackPolicy.MAX_REPEAT, clamped.repeatMinutes)
    }

    @Test
    fun clipsToMaxLength() {
        val policy = TtsPlaybackPolicy(maxLength = 5)
        assertEquals("hello", policy.prepareUtterance("hello world"))
    }

    @Test
    fun stripsEmojisWhenDisabled() {
        val policy = TtsPlaybackPolicy(speakEmojis = false)
        assertEquals("hi there", policy.prepareUtterance("hi 🎉 there"))
    }

    @Test
    fun blankAfterStripIsEmpty() {
        val policy = TtsPlaybackPolicy(speakEmojis = false)
        assertEquals("", policy.prepareUtterance("🎉"))
    }
}

class DeviceStatePolicyTest {
    @Test
    fun defaultAllowsIdlePhone() {
        assertTrue(DeviceStatePolicy().allows(true, false, false, false))
        assertFalse(DeviceStatePolicy().allows(true, false, silentOrVibrate = true, inCall = false))
    }

    @Test
    fun defaultBlocksInCall() {
        assertFalse(DeviceStatePolicy().allows(true, false, false, inCall = true))
    }

    @Test
    fun silentFlagHonored() {
        val policy = DeviceStatePolicy(allowSilentVibrate = false)
        assertFalse(policy.allows(true, false, silentOrVibrate = true, inCall = false))
        assertTrue(policy.allows(true, false, silentOrVibrate = false, inCall = false))
    }

    @Test
    fun screenOffOnlyViaFlags() {
        val policy = DeviceStatePolicy(allowScreenOn = false, allowScreenOff = true)
        assertFalse(policy.allows(screenOn = true, headsetOn = false, false, false))
        assertTrue(policy.allows(screenOn = false, headsetOn = false, false, false))
    }
}

class TtsVoicePickTest {
    @Test
    fun prefersLocalHighQualityVoice() {
        val low = TtsVoiceCandidate("low", "en-US", quality = 200, latency = 100, networkRequired = false)
        val high = TtsVoiceCandidate("high", "en-US", quality = 500, latency = 200, networkRequired = false)
        val net = TtsVoiceCandidate("net", "en-US", quality = 500, latency = 50, networkRequired = true)
        val picked = TtsVoicePick.best(listOf(low, high, net), "en-US")
        assertEquals("high", picked?.name)
    }

    @Test
    fun matchesLanguagePrefixAndFallsBack() {
        val fr = TtsVoiceCandidate("fr", "fr-FR", quality = 400, latency = 80, networkRequired = false)
        val en = TtsVoiceCandidate("en", "en-US", quality = 500, latency = 80, networkRequired = false)
        assertEquals("fr", TtsVoicePick.best(listOf(fr, en), "fr")?.name)
        assertEquals("en", TtsVoicePick.best(listOf(fr, en), "de-DE")?.name)
    }

    @Test
    fun prefersNamedVoiceThenQualityFloor() {
        val low = TtsVoiceCandidate("low", "en-US", quality = 200, latency = 40, networkRequired = false)
        val mid = TtsVoiceCandidate("mid", "en-US", quality = 300, latency = 40, networkRequired = false)
        val high = TtsVoiceCandidate("high", "en-US", quality = 500, latency = 80, networkRequired = false)
        assertEquals("low", TtsVoicePick.best(listOf(low, mid, high), "en-US", preferredName = "low")?.name)
        assertEquals("high", TtsVoicePick.best(listOf(low, mid, high), "en-US", minQuality = 400)?.name)
        assertEquals(null, TtsVoicePick.best(listOf(low, mid), "en-US", minQuality = 400)?.name)
        assertEquals(null, TtsVoicePick.best(listOf(low, mid, high), "en-US", minQuality = 500, preferredName = "low")?.name)
    }
}

class TtsLocaleMenuTest {
    @Test
    fun groupsAccentsUnderLanguage() {
        val tags = listOf("ja-JP", "en-US", "en-GB")
        val ui = java.util.Locale.US
        assertEquals(listOf("en", "ja"), TtsLocaleMenu.languages(tags, ui))
        assertEquals(listOf("en-GB", "en-US"), TtsLocaleMenu.accents(tags, "en", ui))
        assertEquals("Japanese", TtsLocaleMenu.displayLanguage("ja", ui))
        assertEquals("Japan", TtsLocaleMenu.displayAccent("ja-JP", ui))
        assertEquals("jab-local", TtsLocaleMenu.shortName("ja-jp-x-jab-local"))
    }

    @Test
    fun listsLocalVoicesForAccentFirst() {
        val local = TtsVoiceCandidate("ja-local", "ja-JP", 400, 80, networkRequired = false)
        val net = TtsVoiceCandidate("ja-net", "ja-JP", 500, 40, networkRequired = true)
        val en = TtsVoiceCandidate("en", "en-US", 500, 40, networkRequired = false)
        assertEquals(listOf("ja-local", "ja-net"), TtsLocaleMenu.voicesFor(listOf(net, local, en), "ja-JP").map { it.name })
    }
}

class TtsSourceCatalogTest {
    @Test
    fun hidesInstalledOffersAndKeepsHttps() {
        val missing = TtsSourceCatalog.missing(setOf(TtsSourceCatalog.GOOGLE))
        assertEquals(2, missing.size)
        assertTrue(missing.none { it.packageName == TtsSourceCatalog.GOOGLE })
        assertTrue(TtsSourceCatalog.OFFERS.all { it.downloadUrl.startsWith("https://") })
        assertTrue(TtsSourceCatalog.known(TtsSourceCatalog.SHERPA)?.foss == true)
    }
}

class AnnouncementGateDeviceStateTest {
    @Test
    fun deviceStateBlocksInCall() {
        assertFalse(
            AnnouncementGate.allow(
                AppSettings(),
                12 * 60,
                2,
                screenOn = true,
                headsetConnected = false,
                insideSilentGeofence = false,
                inCall = true,
            ),
        )
    }
}
