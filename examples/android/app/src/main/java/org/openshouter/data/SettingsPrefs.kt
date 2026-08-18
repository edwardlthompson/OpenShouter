package org.openshouter.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.openshouter.domain.AppSettings
import org.openshouter.domain.DeviceStatePolicy
import org.openshouter.domain.FilterMode
import org.openshouter.domain.TimeShout
import org.openshouter.domain.TtsFormat
import org.openshouter.domain.TtsPlaybackPolicy
import org.openshouter.domain.AppOverrides
import org.openshouter.domain.MessageChannelPolicy
import org.openshouter.domain.MissedCallPolicy
import org.openshouter.domain.QuietHours
import org.openshouter.domain.TtsStream
import org.openshouter.domain.TtsVoice
import org.openshouter.domain.ShakeThreshold
import org.openshouter.domain.NotificationPolicy

internal object SettingsKeys {
    val ENABLED = booleanPreferencesKey("enabled")
    val NOTIFS = booleanPreferencesKey("notifs")
    val CALLS = booleanPreferencesKey("calls")
    val FORMAT = stringPreferencesKey("format")
    val FILTER = stringPreferencesKey("filter")
    val PACKAGES = stringSetPreferencesKey("packages")
    val QUIET = booleanPreferencesKey("quiet")
    val QUIET_START = intPreferencesKey("quiet_start")
    val QUIET_END = intPreferencesKey("quiet_end")
    val QUIET_DAYS = stringSetPreferencesKey("quiet_days")
    val SCREEN_OFF = booleanPreferencesKey("screen_off")
    val HEADSET = booleanPreferencesKey("headset")
    val SHAKE = booleanPreferencesKey("shake")
    val FLIP = booleanPreferencesKey("flip")
    val MUTE_ON = booleanPreferencesKey("mute_on")
    val MUTE_OFF = booleanPreferencesKey("mute_off")
    val BATT_LOW = booleanPreferencesKey("batt_low")
    val POWER = booleanPreferencesKey("power")
    val BATT_FULL = intPreferencesKey("batt_full")
    val BATT_PCT = intPreferencesKey("batt_pct")
    val SETUP = booleanPreferencesKey("setup")
    val TIME = booleanPreferencesKey("time")
    val TIME_EVERY = intPreferencesKey("time_every")
    val TIME_EXACT = booleanPreferencesKey("time_exact")
    val MSG_ON = booleanPreferencesKey("msg_on")
    val MSG_UNKNOWN = booleanPreferencesKey("msg_unknown")
    val MSG_BODY = booleanPreferencesKey("msg_body")
    val MSG_KNOWN = booleanPreferencesKey("msg_known")
    val MISS_ON = booleanPreferencesKey("miss_on")
    val MISS_UNKNOWN = booleanPreferencesKey("miss_unknown")
    val APP_FORMATS = stringSetPreferencesKey("app_formats")
    val TTS_STREAM = stringPreferencesKey("tts_stream")
    val TTS_DELAY = intPreferencesKey("tts_delay")
    val TTS_MAX = intPreferencesKey("tts_max")
    val TTS_FOCUS = booleanPreferencesKey("tts_focus")
    val TTS_EMOJI = booleanPreferencesKey("tts_emoji")
    val TTS_REPEAT = intPreferencesKey("tts_repeat")
    val TTS_REPEAT_COUNT = intPreferencesKey("tts_repeat_count")
    val TTS_PAUSE = booleanPreferencesKey("tts_pause")
    val TTS_PITCH = intPreferencesKey("tts_pitch")
    val TTS_LANG = stringPreferencesKey("tts_lang")
    val SHAKE_G = intPreferencesKey("shake_g")
    val IGNORE_EMPTY = booleanPreferencesKey("ignore_empty")
    val IGNORE_GROUP = booleanPreferencesKey("ignore_group")
    val IGNORE_REPEATS = booleanPreferencesKey("ignore_repeats")
    val DS_SCREEN_ON = booleanPreferencesKey("ds_screen_on")
    val DS_SCREEN_OFF = booleanPreferencesKey("ds_screen_off")
    val DS_HEADSET_ON = booleanPreferencesKey("ds_headset_on")
    val DS_HEADSET_OFF = booleanPreferencesKey("ds_headset_off")
    val DS_SILENT = booleanPreferencesKey("ds_speak_silent")
    val DS_INCALL = booleanPreferencesKey("ds_incall")
}

internal fun Preferences.toAppSettings(): AppSettings {
    val k = SettingsKeys
    return SettingsSprint13.apply(AppSettings(
        announcerEnabled = this[k.ENABLED] ?: true,
        notificationsEnabled = this[k.NOTIFS] ?: true,
        callsEnabled = this[k.CALLS] ?: true,
        ttsFormat = this[k.FORMAT] ?: TtsFormat.DEFAULT,
        filterMode = runCatching {
            FilterMode.valueOf(this[k.FILTER] ?: FilterMode.BLACKLIST.name)
        }.getOrDefault(FilterMode.BLACKLIST),
        listedPackages = this[k.PACKAGES] ?: emptySet(),
        quietHoursEnabled = this[k.QUIET] ?: false,
        quietStartMinutes = this[k.QUIET_START] ?: 22 * 60,
        quietEndMinutes = this[k.QUIET_END] ?: 7 * 60,
        quietDays = (this[k.QUIET_DAYS] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
            .ifEmpty { QuietHours.ALL_DAYS },
        screenOffOnly = this[k.SCREEN_OFF] ?: false,
        headsetOnly = this[k.HEADSET] ?: false,
        shakeToSilence = this[k.SHAKE] ?: true,
        flipToMute = this[k.FLIP] ?: true,
        muteOnScreenOn = this[k.MUTE_ON] ?: false,
        muteOnScreenOff = this[k.MUTE_OFF] ?: false,
        lowBatteryAlert = this[k.BATT_LOW] ?: true,
        powerConnectAlert = this[k.POWER] ?: true,
        batteryFullPercent = this[k.BATT_FULL] ?: 100,
        batteryLowPercent = this[k.BATT_PCT] ?: 15,
        setupComplete = this[k.SETUP] ?: false,
        timeShoutEnabled = this[k.TIME] ?: false,
        timeShoutIntervalMinutes = this[k.TIME_EVERY] ?: TimeShout.INTERVAL_HOUR,
        timeShoutExact = this[k.TIME_EXACT] ?: true,
        messageChannel = MessageChannelPolicy(
            enabled = this[k.MSG_ON] ?: false,
            speakUnknown = this[k.MSG_UNKNOWN] ?: true,
            speakBody = this[k.MSG_BODY] ?: true,
            knownContactsOnly = this[k.MSG_KNOWN] ?: false,
        ),
        missedCall = MissedCallPolicy(
            enabled = this[k.MISS_ON] ?: false,
            speakUnknown = this[k.MISS_UNKNOWN] ?: true,
        ),
        appFormats = AppOverrides.parse(this[k.APP_FORMATS] ?: emptySet()),
        ttsPlayback = TtsPlaybackPolicy(
            stream = runCatching {
                TtsStream.valueOf(this[k.TTS_STREAM] ?: TtsStream.MEDIA.name)
            }.getOrDefault(TtsStream.MEDIA),
            delaySeconds = this[k.TTS_DELAY] ?: 0,
            maxLength = this[k.TTS_MAX] ?: 0,
            audioFocus = this[k.TTS_FOCUS] ?: true,
            speakEmojis = this[k.TTS_EMOJI] ?: true,
            repeatMinutes = this[k.TTS_REPEAT] ?: 0,
            repeatCount = this[k.TTS_REPEAT_COUNT] ?: 0,
            pauseMedia = this[k.TTS_PAUSE] ?: false,
            voice = TtsVoice(
                pitch = (this[k.TTS_PITCH] ?: 100) / 100f,
                languageTag = this[k.TTS_LANG].orEmpty(),
            ),
        ).clamp(),
        shakeThreshold = ShakeThreshold.clamp((this[k.SHAKE_G] ?: 24) / 10f),
        notificationPolicy = NotificationPolicy(
            ignoreEmpty = this[k.IGNORE_EMPTY] ?: true,
            ignoreGroup = this[k.IGNORE_GROUP] ?: true,
            ignoreRepeats = this[k.IGNORE_REPEATS] ?: true,
        ),
        deviceState = DeviceStatePolicy(
            allowScreenOn = this[k.DS_SCREEN_ON] ?: true,
            allowScreenOff = this[k.DS_SCREEN_OFF] ?: true,
            allowHeadsetOn = this[k.DS_HEADSET_ON] ?: true,
            allowHeadsetOff = this[k.DS_HEADSET_OFF] ?: true,
            allowSilentVibrate = this[k.DS_SILENT] ?: false,
            allowInCall = this[k.DS_INCALL] ?: false,
        ),
    ), this)
}
