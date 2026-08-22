package org.openshouter.backup

import org.openshouter.domain.BatteryPhrases
import org.openshouter.domain.ContactRule
import org.openshouter.domain.DeviceStatePolicy
import org.openshouter.domain.MessageChannelPolicy
import org.openshouter.domain.MissedCallPolicy
import org.openshouter.domain.QuietHours
import org.openshouter.domain.ReminderInterval
import org.openshouter.domain.TimeHourStyle
import org.openshouter.domain.TimeShout
import org.openshouter.domain.TtsFormat
import org.openshouter.domain.TtsPlaybackPolicy
import org.openshouter.domain.TtsVoice

data class LegacyMapped(
    val announcer: Boolean = true,
    val notifications: Boolean = true,
    val calls: Boolean = true,
    val screenOffOnly: Boolean = false,
    val headsetOnly: Boolean = false,
    val shake: Boolean = false,
    val muteOn: Boolean = false,
    val muteOff: Boolean = false,
    val device: DeviceStatePolicy = DeviceStatePolicy(),
    val timeOn: Boolean = false,
    val timeEvery: Int = TimeShout.INTERVAL_HOUR,
    val timeExact: Boolean = true,
    val timeFormat: String = TtsFormat.TIME_DEFAULT,
    val callFormat: String = TtsFormat.CALL_DEFAULT,
    val notifFormat: String = TtsFormat.DEFAULT,
    val messageFormat: String = TtsFormat.MESSAGE_DEFAULT,
    val timeHour: TimeHourStyle = TimeHourStyle.SYSTEM,
    val messages: MessageChannelPolicy = MessageChannelPolicy(),
    val missed: MissedCallPolicy = MissedCallPolicy(),
    val playback: TtsPlaybackPolicy = TtsPlaybackPolicy(),
    val battery: BatteryPhrases = BatteryPhrases(),
    val alsoNotify: Boolean = false,
    val quietOn: Boolean = false,
    val quietStart: Int = 22 * 60,
    val quietEnd: Int = 7 * 60,
    val quietDays: Set<Int> = QuietHours.ALL_DAYS,
    val contacts: ContactRule = ContactRule(),
    val speakApp: Boolean = true,
    val remindersOn: Boolean = true,
    val hasPrefs: Boolean = false,
)

object ShouterLegacyMap {
    fun map(dump: LegacyDump): LegacyMapped {
        val p = dump.prefs
        val screenOff = flag(p, "Ennotiscrnoffonly")
        val headset = flag(p, "Ennotifnvohdphonly")
        val quiet = quietWindow(dump.quietCells)
        return LegacyMapped(
            announcer = flag(p, "Enmstcntrl", true),
            notifications = flag(p, "Ennotifname", true),
            calls = flag(p, "Encallername", true),
            screenOffOnly = screenOff,
            headsetOnly = headset,
            shake = flag(p, "Enmstcntrlshake"),
            muteOn = flag(p, "Enmstcntlscron"),
            muteOff = flag(p, "Enmstcntlscrof"),
            device = DeviceStatePolicy(
                allowScreenOn = !screenOff,
                allowScreenOff = true,
                allowHeadsetOn = true,
                allowHeadsetOff = !headset,
            ),
            timeOn = flag(p, "Entimeshout"),
            timeEvery = TimeShout.normalizeInterval(int(p, "Entssel", TimeShout.INTERVAL_HOUR)),
            timeExact = flag(p, "Enttsexact", true),
            timeFormat = ShouterLegacyPhrases.affix(p["entsimprefix"], null, TtsFormat.TIME_DEFAULT, "%time"),
            callFormat = ShouterLegacyPhrases.affix(p["Ecllerprefix"], p["Ecllersuffix"], TtsFormat.CALL_DEFAULT, "%name"),
            notifFormat = ShouterLegacyPhrases.notif(p),
            messageFormat = ShouterLegacyPhrases.affix(p["Emsgprefix"], p["Emsgsuffix"], TtsFormat.MESSAGE_DEFAULT, "%name: %text"),
            timeHour = hourStyle(p["entmforid"]),
            messages = MessageChannelPolicy(
                enabled = flag(p, "EnMessageShout"),
                speakUnknown = flag(p, "EnMessreadnumunk", true),
                speakBody = flag(p, "EnMessconnetntShout", true),
                knownContactsOnly = flag(p, "EnMessunkconnetnkownout"),
            ),
            missed = MissedCallPolicy(enabled = flag(p, "Encallername", true), speakUnknown = flag(p, "Encallreadnumunk", true)),
            playback = TtsPlaybackPolicy(
                stream = ShouterLegacyChannels.stream(p["Enotifstrm"]),
                repeatCount = ShouterLegacyChannels.repeat(p["Enotifrptcnt"]),
                voice = TtsVoice(languageTag = ShouterLegacyPhrases.langTag(p["pk_setting_tts_lang"])),
            ),
            battery = ShouterLegacyPhrases.battery(p),
            speakApp = flag(p, "Enotifrdapname", true),
            remindersOn = flag(p, "enremasht", true),
            alsoNotify = flag(p, "Eremanoif"),
            quietOn = quiet != null,
            quietStart = quiet?.first ?: 22 * 60,
            quietEnd = quiet?.second ?: 7 * 60,
            quietDays = quiet?.third ?: QuietHours.ALL_DAYS,
            contacts = ContactRule(dump.nicks, dump.blocked),
            hasPrefs = p.isNotEmpty(),
        )
    }

    fun reminderMinutes(type: Int): Int = when (type) {
        0 -> ReminderInterval.HOUR
        1 -> ReminderInterval.DAY
        2 -> ReminderInterval.WEEK
        3 -> ReminderInterval.MONTH
        4 -> ReminderInterval.YEAR
        else -> ReminderInterval.DAY
    }

    internal fun quietWindow(cells: List<Pair<Int, Int>>): Triple<Int, Int, Set<Int>>? {
        if (cells.isEmpty()) return null
        val hours = cells.map { it.first.coerceIn(0, 23) }
        val days = cells.mapNotNull { day ->
            when (day.second) {
                in 1..7 -> day.second
                0 -> 7
                else -> null
            }
        }.toSet().ifEmpty { QuietHours.ALL_DAYS }
        return Triple(hours.min() * 60, ((hours.max() + 1) % 24) * 60, days)
    }

    private fun flag(p: Map<String, String>, key: String, default: Boolean = false): Boolean {
        val raw = p[key] ?: return default
        return ShouterLegacyParse.shoutEnabled(raw)
    }

    private fun int(p: Map<String, String>, key: String, default: Int): Int =
        p[key]?.toIntOrNull() ?: default

    private fun hourStyle(raw: String?): TimeHourStyle = when (raw?.trim()) {
        "1" -> TimeHourStyle.HOUR_12
        "2" -> TimeHourStyle.HOUR_24
        else -> TimeHourStyle.SYSTEM
    }
}
