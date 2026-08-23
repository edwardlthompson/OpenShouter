package org.openshouter.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.openshouter.domain.AppSettings
import org.openshouter.domain.DeviceStatePolicy
import org.openshouter.domain.AppOverrides
import org.openshouter.domain.FilterMode
import org.openshouter.domain.MessageChannelPolicy
import org.openshouter.domain.MissedCallPolicy
import org.openshouter.domain.NotificationPolicy
import org.openshouter.domain.ShakeThreshold
import org.openshouter.domain.TimeShout
import org.openshouter.domain.TtsFormat
import org.openshouter.domain.TtsPlaybackPolicy

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val settings: Flow<AppSettings> = context.osDataStore.data.map { it.toAppSettings() }

    suspend fun snapshot(): AppSettings = settings.first()
    suspend fun setEnabled(value: Boolean) = context.osDataStore.edit { it[SettingsKeys.ENABLED] = value }
    suspend fun setFormat(value: String) = context.osDataStore.edit { it[SettingsKeys.FORMAT] = TtsFormat.clamp(value) }

    suspend fun setFilterMode(mode: FilterMode) {
        context.osDataStore.edit { it[SettingsKeys.FILTER] = mode.name }
    }

    suspend fun setListedPackages(packages: Set<String>) {
        context.osDataStore.edit { it[SettingsKeys.PACKAGES] = packages }
    }

    suspend fun setQuietHours(enabled: Boolean, start: Int, end: Int, days: Set<Int>) {
        context.osDataStore.edit {
            it[SettingsKeys.QUIET] = enabled
            it[SettingsKeys.QUIET_START] = start
            it[SettingsKeys.QUIET_END] = end
            it[SettingsKeys.QUIET_DAYS] = days.map(Int::toString).toSet()
        }
    }

    suspend fun setAudioGate(screenOffOnly: Boolean, headsetOnly: Boolean) {
        context.osDataStore.edit {
            it[SettingsKeys.SCREEN_OFF] = screenOffOnly
            it[SettingsKeys.HEADSET] = headsetOnly
        }
    }

    suspend fun setGestures(shake: Boolean, flip: Boolean, muteOn: Boolean, muteOff: Boolean) {
        context.osDataStore.edit {
            it[SettingsKeys.SHAKE] = shake
            it[SettingsKeys.FLIP] = flip
            it[SettingsKeys.MUTE_ON] = muteOn
            it[SettingsKeys.MUTE_OFF] = muteOff
        }
    }

    suspend fun setCalls(enabled: Boolean) {
        context.osDataStore.edit { it[SettingsKeys.CALLS] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.osDataStore.edit { it[SettingsKeys.NOTIFS] = enabled }
    }

    suspend fun setSetupComplete(value: Boolean) {
        context.osDataStore.edit { it[SettingsKeys.SETUP] = value }
    }

    suspend fun setTimeShout(enabled: Boolean, intervalMinutes: Int, exact: Boolean = true) {
        context.osDataStore.edit {
            it[SettingsKeys.TIME] = enabled
            it[SettingsKeys.TIME_EVERY] = TimeShout.normalizeInterval(intervalMinutes)
            it[SettingsKeys.TIME_EXACT] = exact
        }
    }

    suspend fun setMessageChannel(policy: MessageChannelPolicy) {
        context.osDataStore.edit {
            it[SettingsKeys.MSG_ON] = policy.enabled
            it[SettingsKeys.MSG_UNKNOWN] = policy.speakUnknown
            it[SettingsKeys.MSG_BODY] = policy.speakBody
            it[SettingsKeys.MSG_KNOWN] = policy.knownContactsOnly
        }
    }

    suspend fun setAppFormat(packageName: String, format: String) {
        context.osDataStore.edit { prefs ->
            val current = AppOverrides.parse(prefs[SettingsKeys.APP_FORMATS] ?: emptySet()).toMutableMap()
            if (format.isBlank()) current.remove(packageName) else current[packageName] = format
            prefs[SettingsKeys.APP_FORMATS] = AppOverrides.encode(current)
        }
    }

    suspend fun setMissedCall(policy: MissedCallPolicy) {
        context.osDataStore.edit {
            it[SettingsKeys.MISS_ON] = policy.enabled
            it[SettingsKeys.MISS_UNKNOWN] = policy.speakUnknown
        }
    }

    suspend fun setTtsPlayback(policy: TtsPlaybackPolicy) {
        val p = policy.clamp()
        context.osDataStore.edit {
            it[SettingsKeys.TTS_STREAM] = p.stream.name
            it[SettingsKeys.TTS_DELAY] = p.delaySeconds
            it[SettingsKeys.TTS_MAX] = p.maxLength
            it[SettingsKeys.TTS_FOCUS] = p.audioFocus
            it[SettingsKeys.TTS_EMOJI] = p.speakEmojis
            it[SettingsKeys.TTS_REPEAT] = p.repeatMinutes
            it[SettingsKeys.TTS_REPEAT_COUNT] = p.repeatCount
            it[SettingsKeys.TTS_PAUSE] = p.pauseMedia
            it[SettingsKeys.TTS_PITCH] = (p.voice.pitch * 100).toInt()
            it[SettingsKeys.TTS_LANG] = p.voice.languageTag
        }
    }

    suspend fun setDeviceState(policy: DeviceStatePolicy) {
        context.osDataStore.edit {
            it[SettingsKeys.DS_SCREEN_ON] = policy.allowScreenOn
            it[SettingsKeys.DS_SCREEN_OFF] = policy.allowScreenOff
            it[SettingsKeys.DS_HEADSET_ON] = policy.allowHeadsetOn
            it[SettingsKeys.DS_HEADSET_OFF] = policy.allowHeadsetOff
            it[SettingsKeys.DS_SILENT] = policy.allowSilentVibrate
            it[SettingsKeys.DS_INCALL] = policy.allowInCall
        }
    }

    suspend fun setShakeThreshold(g: Float) = context.osDataStore.edit {
        it[SettingsKeys.SHAKE_G] = (ShakeThreshold.clamp(g) * 10).toInt()
    }

    suspend fun setNotificationPolicy(p: NotificationPolicy) = context.osDataStore.edit {
        it[SettingsKeys.IGNORE_EMPTY] = p.ignoreEmpty
        it[SettingsKeys.IGNORE_GROUP] = p.ignoreGroup
        it[SettingsKeys.IGNORE_REPEATS] = p.ignoreRepeats
        it[SettingsSprint17.COLLAPSE_REPEATS] = p.collapseRepeats
        it[SettingsSprint17.MIN_IMPORTANCE] = p.minImportance.name
        it[SettingsSprint17.DND_PRIORITY] = p.dndPriorityOnly
    }
}
