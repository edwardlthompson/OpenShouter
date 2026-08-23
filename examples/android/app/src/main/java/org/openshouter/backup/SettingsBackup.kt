package org.openshouter.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject
import org.openshouter.domain.AppSettings
import org.openshouter.domain.AppSpeakRule

object SettingsBackup {
    fun toZip(settings: AppSettings, rules: List<AppSpeakRule>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(BackupAllowlist.SETTINGS))
            zip.write(settingsJson(settings).toByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry(BackupAllowlist.APP_SPEAK))
            zip.write(rulesJson(rules).toByteArray())
            zip.closeEntry()
        }
        return out.toByteArray()
    }

    fun fromZip(bytes: ByteArray): Pair<JSONObject, List<AppSpeakRule>> {
        if (bytes.size > BackupLimits.MAX_ZIP_BYTES) return JSONObject() to emptyList()
        var settings = JSONObject()
        var rules = emptyList<AppSpeakRule>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name.substringAfterLast('/')
                if (BackupAllowlist.allowed(name)) {
                    val chunk = BackupLimits.readBounded(zip, BackupLimits.MAX_ENTRY_BYTES)
                    if (chunk != null) {
                        val text = chunk.decodeToString()
                        if (name == BackupAllowlist.SETTINGS) settings = JSONObject(text)
                        if (name == BackupAllowlist.APP_SPEAK) rules = parseRules(text)
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return settings to rules
    }

    private fun settingsJson(settings: AppSettings): String = JSONObject()
        .put("announcerEnabled", settings.announcerEnabled)
        .put("notificationsEnabled", settings.notificationsEnabled)
        .put("callsEnabled", settings.callsEnabled)
        .put("ttsFormat", settings.ttsFormat)
        .put("timeShoutEnabled", settings.timeShoutEnabled)
        .put("timeShoutIntervalMinutes", settings.timeShoutIntervalMinutes)
        .put("timeShoutExact", settings.timeShoutExact)
        .toString()

    private fun rulesJson(rules: List<AppSpeakRule>): String {
        val arr = JSONArray()
        rules.forEach { rule ->
            arr.put(
                JSONObject()
                    .put("packageName", rule.packageName)
                    .put("speakAppName", rule.speakAppName)
                    .put("speakNotification", rule.speakNotification),
            )
        }
        return arr.toString()
    }

    private fun parseRules(text: String): List<AppSpeakRule> {
        val arr = JSONArray(text)
        val limit = minOf(arr.length(), BackupLimits.MAX_RULES)
        return buildList {
            for (i in 0 until limit) {
                val obj = arr.getJSONObject(i)
                val pkg = obj.optString("packageName")
                if (!ShouterLegacyParse.validPackage(pkg)) continue
                add(
                    AppSpeakRule(
                        pkg,
                        obj.optBoolean("speakAppName"),
                        obj.optBoolean("speakNotification"),
                    ),
                )
            }
        }
    }
}
