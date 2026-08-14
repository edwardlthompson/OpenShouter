package org.openshouter.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.openshouter.domain.AppSettings
import org.openshouter.domain.FilterMode
import org.openshouter.domain.TtsFormat
import org.openshouter.domain.TimeShout

private val Context.dataStore by preferencesDataStore("openshouter")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            announcerEnabled = prefs[Keys.ENABLED] ?: true,
            notificationsEnabled = prefs[Keys.NOTIFS] ?: true,
            callsEnabled = prefs[Keys.CALLS] ?: true,
            ttsFormat = prefs[Keys.FORMAT] ?: TtsFormat.DEFAULT,
            filterMode = runCatching {
                FilterMode.valueOf(prefs[Keys.FILTER] ?: FilterMode.BLACKLIST.name)
            }.getOrDefault(FilterMode.BLACKLIST),
            listedPackages = prefs[Keys.PACKAGES] ?: emptySet(),
            quietHoursEnabled = prefs[Keys.QUIET] ?: false,
            quietStartMinutes = prefs[Keys.QUIET_START] ?: 22 * 60,
            quietEndMinutes = prefs[Keys.QUIET_END] ?: 7 * 60,
            quietDays = (prefs[Keys.QUIET_DAYS] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
                .ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) },
            screenOffOnly = prefs[Keys.SCREEN_OFF] ?: false,
            headsetOnly = prefs[Keys.HEADSET] ?: false,
            shakeToSilence = prefs[Keys.SHAKE] ?: true,
            flipToMute = prefs[Keys.FLIP] ?: true,
            muteOnScreenOn = prefs[Keys.MUTE_ON] ?: false,
            muteOnScreenOff = prefs[Keys.MUTE_OFF] ?: false,
            lowBatteryAlert = prefs[Keys.BATT_LOW] ?: true,
            powerConnectAlert = prefs[Keys.POWER] ?: true,
            batteryFullPercent = prefs[Keys.BATT_FULL] ?: 100,
            batteryLowPercent = prefs[Keys.BATT_PCT] ?: 15,
            setupComplete = prefs[Keys.SETUP] ?: false,
            timeShoutEnabled = prefs[Keys.TIME] ?: false,
            timeShoutIntervalMinutes = prefs[Keys.TIME_EVERY] ?: TimeShout.INTERVAL_HOUR,
        )
    }

    suspend fun snapshot(): AppSettings = settings.first()

    suspend fun setEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.ENABLED] = value }
    }

    suspend fun setFormat(value: String) {
        context.dataStore.edit { it[Keys.FORMAT] = value }
    }

    suspend fun setFilterMode(mode: FilterMode) {
        context.dataStore.edit { it[Keys.FILTER] = mode.name }
    }

    suspend fun setListedPackages(packages: Set<String>) {
        context.dataStore.edit { it[Keys.PACKAGES] = packages }
    }

    suspend fun setQuietHours(enabled: Boolean, start: Int, end: Int, days: Set<Int>) {
        context.dataStore.edit {
            it[Keys.QUIET] = enabled
            it[Keys.QUIET_START] = start
            it[Keys.QUIET_END] = end
            it[Keys.QUIET_DAYS] = days.map(Int::toString).toSet()
        }
    }

    suspend fun setAudioGate(screenOffOnly: Boolean, headsetOnly: Boolean) {
        context.dataStore.edit {
            it[Keys.SCREEN_OFF] = screenOffOnly
            it[Keys.HEADSET] = headsetOnly
        }
    }

    suspend fun setGestures(shake: Boolean, flip: Boolean, muteOn: Boolean, muteOff: Boolean) {
        context.dataStore.edit {
            it[Keys.SHAKE] = shake
            it[Keys.FLIP] = flip
            it[Keys.MUTE_ON] = muteOn
            it[Keys.MUTE_OFF] = muteOff
        }
    }

    suspend fun setCalls(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CALLS] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFS] = enabled }
    }

    suspend fun setSetupComplete(value: Boolean) {
        context.dataStore.edit { it[Keys.SETUP] = value }
    }

    suspend fun setTimeShout(enabled: Boolean, intervalMinutes: Int) {
        context.dataStore.edit {
            it[Keys.TIME] = enabled
            it[Keys.TIME_EVERY] = TimeShout.normalizeInterval(intervalMinutes)
        }
    }

    private object Keys {
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
    }
}
