package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySpeakTest {
    private val rules = mapOf(
        "sms.app" to AppSpeakRule("sms.app", speakAppName = true, speakNotification = false),
    )

    @Test
    fun shoutingFollowsActiveAppSpeakRule() {
        assertTrue(HistorySpeak.isShouting("sms.app", rules))
        assertFalse(HistorySpeak.isShouting("mail.app", rules))
    }

    @Test
    fun toggleOnSetsBothSpeakFlags() {
        assertEquals(true to true, HistorySpeak.enabledFlags(true))
        assertEquals(false to false, HistorySpeak.enabledFlags(false))
    }
}
