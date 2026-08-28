package org.openshouter.backup

import android.content.Context
import org.openshouter.data.ReminderEntity
import org.openshouter.domain.AppSettings
import org.openshouter.domain.AppSpeakRule
import org.openshouter.domain.CallRepeatMode
import org.openshouter.domain.ReminderInterval
import org.openshouter.reminder.ReminderAlarms
import org.openshouter.service.OpenShouterEntryPoint

object BackupImport {
    suspend fun applyBytes(
        ep: OpenShouterEntryPoint,
        current: AppSettings,
        bytes: ByteArray,
        context: Context,
    ): Int = runCatching {
        ShouterLegacy.dumpFromBytes(bytes)?.let { return@runCatching applyDump(ep, it, context) }
        applyZip(ep, current, bytes)
    }.getOrDefault(0)

    suspend fun applyInstalled(ep: OpenShouterEntryPoint, context: Context): Int =
        applyDump(ep, ShouterLegacy.queryBest(context.contentResolver), context)

    suspend fun applyRules(ep: OpenShouterEntryPoint, rules: List<AppSpeakRule>): Int {
        rules.forEach { ep.appSpeak().set(it.packageName, it.speakAppName, it.speakNotification) }
        return rules.size
    }

    suspend fun applyDump(ep: OpenShouterEntryPoint, dump: LegacyDump, context: Context): Int {
        val mapped = ShouterLegacyMap.map(dump)
        applyRules(ep, dump.rules.map { it.copy(speakAppName = mapped.speakApp) })
        val settings = ep.settings()
        if (mapped.hasPrefs) {
            settings.setEnabled(mapped.announcer)
            settings.setNotifications(mapped.notifications)
            settings.setCalls(mapped.calls)
            settings.setAudioGate(mapped.screenOffOnly, mapped.headsetOnly)
            settings.setGestures(mapped.shake, false, mapped.muteOn, mapped.muteOff)
            settings.setTimeShout(mapped.timeOn, mapped.timeEvery, mapped.timeExact)
            settings.setFormat(mapped.notifFormat)
            settings.setDeviceState(mapped.device)
            settings.setMessageChannel(mapped.messages)
            settings.setMissedCall(mapped.missed)
            settings.setTtsPlayback(mapped.playback)
            val s13 = ep.sprint13()
            s13.setCallFormat(mapped.callFormat)
            s13.setMessageFormat(mapped.messageFormat)
            s13.setTimeFormat(mapped.timeFormat)
            s13.setTimeHourStyle(mapped.timeHour)
            s13.setBatteryPhrases(mapped.battery)
            s13.setChannelStates(ShouterLegacyChannels.states(dump.prefs))
        }
        if (dump.nicks.isNotEmpty() || dump.blocked.isNotEmpty()) {
            ep.sprint13().setContactRule(mapped.contacts)
        }
        if (mapped.quietOn) {
            settings.setQuietHours(true, mapped.quietStart, mapped.quietEnd, mapped.quietDays)
        }
        insertReminders(ep, dump, mapped.alsoNotify, mapped.remindersOn, context)
        return dump.itemCount
    }

    private suspend fun insertReminders(
        ep: OpenShouterEntryPoint,
        dump: LegacyDump,
        alsoNotify: Boolean,
        remindersOn: Boolean,
        context: Context,
    ): Int {
        var n = 0
        val now = System.currentTimeMillis()
        val existing = ep.reminders().texts().mapNotNull { ReminderEntity.normalizeText(it) }.toSet()
        dump.reminders.forEach { row ->
            val text = ReminderEntity.normalizeText(row.text) ?: return@forEach
            if (text in existing) return@forEach
            val interval = ShouterLegacyMap.reminderMinutes(row.type)
            val next = if (row.startMillis > now) row.startMillis else ReminderInterval.nextAt(now, interval)
            val entity = ReminderEntity(
                text = text,
                intervalMinutes = interval,
                nextAtMillis = next,
                enabled = row.enabled && remindersOn,
                alsoNotify = alsoNotify,
            )
            val id = ep.reminders().insert(entity)
            ReminderAlarms.sync(context, ep.alarms(), listOf(entity.copy(id = id)), exact = true)
            n++
        }
        return n
    }

    private suspend fun applyZip(ep: OpenShouterEntryPoint, current: AppSettings, bytes: ByteArray): Int {
        val (json, rules) = SettingsBackup.fromZip(bytes)
        ep.settings().setEnabled(json.optBoolean("announcerEnabled", true))
        ep.settings().setNotifications(json.optBoolean("notificationsEnabled", true))
        ep.settings().setCalls(json.optBoolean("callsEnabled", true))
        ep.settings().setFormat(json.optString("ttsFormat", current.ttsFormat))
        ep.settings().setTimeShout(
            json.optBoolean("timeShoutEnabled", false),
            json.optInt("timeShoutIntervalMinutes", current.timeShoutIntervalMinutes),
            json.optBoolean("timeShoutExact", true),
        )
        json.optJSONObject("callRepeatModes")?.let { obj ->
            val map = buildMap {
                obj.keys().forEach { pkg ->
                    if (pkg.isBlank()) return@forEach
                    val mode = runCatching { CallRepeatMode.valueOf(obj.optString(pkg)) }.getOrNull()
                        ?: CallRepeatMode.ONCE
                    put(pkg, mode)
                }
            }
            ep.sprint13().setCallRepeatModes(map)
        }
        return applyRules(ep, rules)
    }
}
