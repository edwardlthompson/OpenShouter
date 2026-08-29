package org.openshouter.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import org.openshouter.domain.AppSpeakRule

class NotificationUtteranceTest {
    private val rule = AppSpeakRule("sms.app", speakAppName = true, speakNotification = true)

    @Test
    fun cooldownOmitsAppNameAndDuplicateTitle() {
        val spoken = NotificationUtterance.build(
            rule, "sms.app", true, "%app: %title - %text",
            "Messages", "Messages", "Photo", emptyMap(), includeAppName = false,
        )
        assertEquals("Photo", spoken)
    }

    @Test
    fun firstShoutKeepsAppName() {
        val spoken = NotificationUtterance.build(
            rule, "sms.app", true, "%app: %title - %text",
            "Messages", "Jane", "Photo", emptyMap(), includeAppName = true,
        )
        assertEquals("Messages: Jane - Photo", spoken)
    }

    @Test
    fun nameOnlyIsSilentDuringCooldown() {
        val nameOnly = AppSpeakRule("sms.app", speakAppName = true, speakNotification = false)
        assertEquals(
            "",
            NotificationUtterance.build(
                nameOnly, "sms.app", false, "%app",
                "Messages", "Jane", "Photo", emptyMap(), includeAppName = false,
            ),
        )
    }
}
