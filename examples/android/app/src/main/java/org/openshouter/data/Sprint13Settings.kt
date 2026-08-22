package org.openshouter.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.domain.AppOverride
import org.openshouter.domain.AppOverrides
import org.openshouter.domain.BatteryPhrases
import org.openshouter.domain.ChannelDeviceState
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ContactRule
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.TimeHourStyle
import org.openshouter.domain.TtsFormat

@Singleton
class Sprint13Settings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun setContactRule(rule: ContactRule) = context.osDataStore.edit {
        it[SettingsSprint13.CONTACTS] = ContactRule.encode(rule)
    }

    suspend fun setChannelStates(map: Map<ShoutChannel, ChannelDeviceState>) = context.osDataStore.edit {
        it[SettingsSprint13.CHANNELS] = ChannelStates.encode(map)
    }

    suspend fun setBatteryPhrases(phrases: BatteryPhrases) = context.osDataStore.edit {
        it[SettingsSprint13.BATT_ON] = phrases.enabled.map { sit -> sit.name }.toSet()
        it[SettingsSprint13.BATT_LOW_P] = phrases.low
        it[SettingsSprint13.BATT_FULL_P] = phrases.full
        it[SettingsSprint13.BATT_CONN_P] = phrases.connected
        it[SettingsSprint13.BATT_DISC_P] = phrases.disconnected
        it[SettingsSprint13.BATT_LEVEL_P] = phrases.level
    }

    suspend fun setCallFormat(value: String) = context.osDataStore.edit {
        it[SettingsSprint13.CALL_FMT] = value.ifBlank { TtsFormat.CALL_DEFAULT }
    }

    suspend fun setMessageFormat(value: String) = context.osDataStore.edit {
        it[SettingsSprint13.MSG_FMT] = value.ifBlank { TtsFormat.MESSAGE_DEFAULT }
    }

    suspend fun setTimeFormat(value: String) = context.osDataStore.edit {
        it[SettingsSprint13.TIME_FMT] = value.ifBlank { TtsFormat.TIME_DEFAULT }
    }

    suspend fun setTimeHourStyle(style: TimeHourStyle) = context.osDataStore.edit {
        it[SettingsSprint13.TIME_HOUR] = style.name
    }

    suspend fun setOverride(row: AppOverride) = context.osDataStore.edit { prefs ->
        val current = AppOverrides.parseFull(prefs[SettingsKeys.APP_FORMATS] ?: emptySet()).toMutableMap()
        if (row.packageName.isBlank()) return@edit
        current[row.packageName] = row
        prefs[SettingsKeys.APP_FORMATS] = AppOverrides.encodeFull(current)
    }
}
