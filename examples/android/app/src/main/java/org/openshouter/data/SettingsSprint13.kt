package org.openshouter.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.openshouter.domain.AppOverrides
import org.openshouter.domain.AppSettings
import org.openshouter.domain.BatteryPhrases
import org.openshouter.domain.BatterySituation
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ContactRule
import org.openshouter.domain.TimeHourStyle
import org.openshouter.domain.TtsFormat

internal object SettingsSprint13 {
    val CONTACTS = stringSetPreferencesKey("contacts")
    val CHANNELS = stringSetPreferencesKey("channels")
    val BATT_ON = stringSetPreferencesKey("batt_on")
    val BATT_LOW_P = stringPreferencesKey("batt_low_p")
    val BATT_FULL_P = stringPreferencesKey("batt_full_p")
    val BATT_CONN_P = stringPreferencesKey("batt_conn_p")
    val BATT_DISC_P = stringPreferencesKey("batt_disc_p")
    val CALL_FMT = stringPreferencesKey("call_fmt")
    val MSG_FMT = stringPreferencesKey("msg_fmt")
    val TIME_FMT = stringPreferencesKey("time_fmt")
    val TIME_HOUR = stringPreferencesKey("time_hour")

    fun apply(base: AppSettings, prefs: Preferences): AppSettings {
        val enabled = (prefs[BATT_ON] ?: emptySet()).mapNotNull {
            runCatching { BatterySituation.valueOf(it) }.getOrNull()
        }.toSet().ifEmpty { BatterySituation.entries.toSet() }
        return base.copy(
            contactRule = ContactRule.parse(prefs[CONTACTS] ?: emptySet()),
            channelStates = ChannelStates.parse(prefs[CHANNELS] ?: emptySet()),
            batteryPhrases = BatteryPhrases(
                enabled = enabled,
                low = prefs[BATT_LOW_P] ?: BatteryPhrases.DEFAULT_LOW,
                full = prefs[BATT_FULL_P] ?: BatteryPhrases.DEFAULT_FULL,
                connected = prefs[BATT_CONN_P] ?: BatteryPhrases.DEFAULT_CONNECTED,
                disconnected = prefs[BATT_DISC_P] ?: BatteryPhrases.DEFAULT_DISCONNECTED,
            ),
            callFormat = prefs[CALL_FMT] ?: TtsFormat.CALL_DEFAULT,
            messageFormat = prefs[MSG_FMT] ?: TtsFormat.MESSAGE_DEFAULT,
            timeFormat = prefs[TIME_FMT] ?: TtsFormat.TIME_DEFAULT,
            timeHourStyle = TimeHourStyle.parse(prefs[TIME_HOUR]),
            appOverrides = AppOverrides.parseFull(prefs[SettingsKeys.APP_FORMATS] ?: emptySet()),
        )
    }
}
