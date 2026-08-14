package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSpeakPolicyTest {
    private val template = "%app: %title - %text"

    @Test
    fun unlistedAppIsSilent() {
        assertEquals(null, AppSpeakPolicy.ruleFor("a.b", emptyMap()))
    }

    @Test
    fun bothOffIsSilent() {
        val rule = AppSpeakRule("a.b", speakAppName = false, speakNotification = false)
        assertEquals(null, AppSpeakPolicy.ruleFor("a.b", mapOf("a.b" to rule)))
    }

    @Test
    fun nameOnlySpeaksLabel() {
        val rule = AppSpeakRule("sms.app", speakAppName = true, speakNotification = false)
        assertEquals(
            "Messages",
            AppSpeakPolicy.utterance(rule, template, "Messages", "Ada", "Hello"),
        )
    }

    @Test
    fun notificationOnlyOmitsAppLabel() {
        val rule = AppSpeakRule("sms.app", speakAppName = false, speakNotification = true)
        assertEquals(
            "Ada - Hello",
            AppSpeakPolicy.utterance(rule, template, "Messages", "Ada", "Hello"),
        )
    }

    @Test
    fun bothUsesFullTemplate() {
        val rule = AppSpeakRule("sms.app", speakAppName = true, speakNotification = true)
        assertEquals(
            "Messages: Ada - Hello",
            AppSpeakPolicy.utterance(rule, template, "Messages", "Ada", "Hello"),
        )
    }
}
