package org.openshouter.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import org.openshouter.domain.AppSettings

internal object SettingsSprint15 {
    val CALENDAR = booleanPreferencesKey("calendar_shout")
    val BT_CONN = booleanPreferencesKey("bt_connect")
    val BT_BATT = booleanPreferencesKey("bt_battery")

    fun apply(base: AppSettings, prefs: Preferences): AppSettings = base.copy(
        calendarShoutEnabled = prefs[CALENDAR] ?: false,
        bluetoothConnectAlert = prefs[BT_CONN] ?: false,
        bluetoothBatteryAlert = prefs[BT_BATT] ?: false,
    )
}
