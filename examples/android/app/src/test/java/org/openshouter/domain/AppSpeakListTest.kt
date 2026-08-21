package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSpeakListTest {
    private val rules = mapOf(
        "sms.app" to AppSpeakRule("sms.app", speakAppName = true, speakNotification = true),
        "mail.app" to AppSpeakRule("mail.app", speakAppName = true, speakNotification = false),
    )

    @Test
    fun searchMatchesLabelOrPackage() {
        assertTrue(AppSpeakList.matches("Messages", "sms.app", "mess"))
        assertTrue(AppSpeakList.matches("Messages", "sms.app", "SMS."))
        assertFalse(AppSpeakList.matches("Messages", "sms.app", "mail"))
        assertTrue(AppSpeakList.matches("Messages", "sms.app", "  "))
    }

    @Test
    fun selectedOnlyKeepsActiveApps() {
        assertTrue(AppSpeakList.include("Messages", "sms.app", "", selectedOnly = true, rules))
        assertTrue(AppSpeakList.include("Mail", "mail.app", "", selectedOnly = true, rules))
        assertFalse(AppSpeakList.include("Clock", "clock.app", "", selectedOnly = true, rules))
        assertTrue(AppSpeakList.include("Clock", "clock.app", "", selectedOnly = false, rules))
    }

    @Test
    fun selectedFilterStillHonorsSearch() {
        assertFalse(AppSpeakList.include("Messages", "sms.app", "mail", selectedOnly = true, rules))
        assertTrue(AppSpeakList.include("Mail", "mail.app", "mail", selectedOnly = true, rules))
    }

    @Test
    fun allSelectedIsFalseUntilEveryVisibleAppIsActive() {
        assertFalse(AppSpeakList.allSelected(emptyList(), rules))
        assertFalse(AppSpeakList.allSelected(listOf("sms.app", "clock.app"), rules))
        assertTrue(AppSpeakList.allSelected(listOf("sms.app", "mail.app"), rules))
    }
}
