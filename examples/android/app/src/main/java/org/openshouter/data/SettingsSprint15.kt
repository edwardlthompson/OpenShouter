package org.openshouter.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import org.openshouter.domain.AppSettings

internal object SettingsSprint15 {
    val CALENDAR = booleanPreferencesKey("calendar_shout")
    val BT_CONN = booleanPreferencesKey("bt_connect")
    val BT_BATT = booleanPreferencesKey("bt_battery")
    val HANGUP_DUR = booleanPreferencesKey("call_hangup_dur")
    val CALL_WAITING = booleanPreferencesKey("call_waiting")
    val CONF_HINT = booleanPreferencesKey("call_conf_hint")

    fun apply(base: AppSettings, prefs: Preferences): AppSettings = base.copy(
        calendarShoutEnabled = prefs[CALENDAR] ?: false,
        bluetoothConnectAlert = prefs[BT_CONN] ?: false,
        bluetoothBatteryAlert = prefs[BT_BATT] ?: false,
        telephonyExtras = base.telephonyExtras.copy(
            speakHangupDuration = prefs[HANGUP_DUR] ?: false,
            callWaitingEnabled = prefs[CALL_WAITING] ?: true,
            conferenceHintEnabled = prefs[CONF_HINT] ?: true,
        ),
    )
}
