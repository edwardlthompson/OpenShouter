package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsFormatTest {
    @Test
    fun rendersNotificationTemplate() {
        val spoken = TtsFormat.notification(
            "%app: %title - %text",
            "Messages",
            "Ada",
            "On my way",
        )
        assertEquals("Messages: Ada - On my way", spoken)
    }

    @Test
    fun stripsEmptyTrailingDash() {
        val spoken = TtsFormat.notification("%title - %text", "App", "Title", "")
        assertEquals("Title", spoken)
    }

    @Test
    fun stripsLeadingColonWhenAppBlank() {
        val spoken = TtsFormat.notification("%app: %title - %text", "", "Ada", "Hi")
        assertEquals("Ada - Hi", spoken)
    }

    @Test
    fun incomingCallPhrase() {
        assertEquals("Incoming call from Ada", TtsFormat.incomingCall("Ada"))
    }
}

class AppFilterTest {
    @Test
    fun blacklistSkipsListedPackage() {
        val settings = AppSettings(
            filterMode = FilterMode.BLACKLIST,
            listedPackages = setOf("spam.app"),
        )
        assertFalse(AppFilter.allows("spam.app", settings))
        assertTrue(AppFilter.allows("ok.app", settings))
    }

    @Test
    fun whitelistRequiresListing() {
        val settings = AppSettings(
            filterMode = FilterMode.WHITELIST,
            listedPackages = setOf("ok.app"),
        )
        assertTrue(AppFilter.allows("ok.app", settings))
        assertFalse(AppFilter.allows("other.app", settings))
    }
}

class RegexFilterTest {
    @Test
    fun ignoreDropsMatchingText() {
        val rules = listOf(RegexRule("otp|verification", RegexAction.IGNORE))
        assertEquals(null, RegexFilter.apply("Your OTP is 123", rules))
    }

    @Test
    fun replaceRewritesPhrase() {
        val rules = listOf(RegexRule("https?://\\S+", RegexAction.REPLACE, "link"))
        assertEquals("see link", RegexFilter.apply("see https://example.com", rules))
    }

    @Test
    fun skipsBlankAndOversizedPatterns() {
        val huge = "a".repeat(RegexFilter.MAX_PATTERN + 1)
        val rules = listOf(
            RegexRule("", RegexAction.IGNORE),
            RegexRule(huge, RegexAction.IGNORE),
        )
        assertEquals("keep me", RegexFilter.apply("keep me", rules))
    }
}

class GeoFenceTest {
    @Test
    fun insideSmallRadius() {
        val home = GeoPlace(label = "Home", latitude = 40.0, longitude = -74.0, radiusMeters = 100f)
        assertTrue(GeoFence.isInside(home, 40.0, -74.0))
        assertFalse(GeoFence.isInside(home, 41.0, -74.0))
    }
}
