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

    @Test
    fun fromZipRejectsOversizedPayload() {
        val huge = ByteArray(BackupLimits.MAX_ZIP_BYTES + 1)
        val (settings, rules) = SettingsBackup.fromZip(huge)
        assertEquals(0, settings.length())
        assertTrue(rules.isEmpty())
    }

    @Test
    fun parseRulesSkipsInvalidPackages() {
        val bad = listOf(AppSpeakRule("not-a-package", speakAppName = true, speakNotification = true))
        val good = listOf(AppSpeakRule("com.example.ok", speakAppName = true, speakNotification = false))
        val (_, restored) = SettingsBackup.fromZip(SettingsBackup.toZip(AppSettings(), bad + good))
        assertEquals(1, restored.size)
        assertEquals("com.example.ok", restored[0].packageName)
    }

    @Test
    fun readBoundedRejectsOverMax() {
        val stream = "hello world".byteInputStream()
        assertEquals(null, BackupLimits.readBounded(stream, 4))
        assertEquals("hi", BackupLimits.readBounded("hi".byteInputStream(), 4)?.decodeToString())
    }
}
