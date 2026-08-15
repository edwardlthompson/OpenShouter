package org.openshouter.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.openshouter.domain.AppSettings
import org.openshouter.domain.AppSpeakRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class SettingsBackupTest {
    @Test
    fun allowlistExcludesHistory() {
        assertTrue(BackupAllowlist.allowed("settings.json"))
        assertTrue(BackupAllowlist.allowed("app_speak_rules.json"))
        assertFalse(BackupAllowlist.allowed("notification_history.json"))
        assertFalse(BackupAllowlist.allowed("history.json"))
    }

    @Test
    fun zipRoundTripOmitsHistoryPayloads() {
        val rules = listOf(AppSpeakRule("pkg.one", speakAppName = true, speakNotification = false))
        val bytes = SettingsBackup.toZip(AppSettings(ttsFormat = "%app %text"), rules)
        val zipText = String(bytes)
        assertFalse(zipText.contains("notification_history"))
        val (settings, restored) = SettingsBackup.fromZip(bytes)
        assertEquals("%app %text", settings.getString("ttsFormat"))
        assertEquals(1, restored.size)
        assertEquals("pkg.one", restored[0].packageName)
        assertTrue(restored[0].speakAppName)
    }
}
