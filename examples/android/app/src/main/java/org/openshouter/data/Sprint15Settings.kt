package org.openshouter.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Sprint15Settings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun setCalendar(enabled: Boolean) = context.osDataStore.edit {
        it[SettingsSprint15.CALENDAR] = enabled
    }

    suspend fun setCalendarLookahead(minutes: Int) = context.osDataStore.edit {
        it[SettingsSprint17.CAL_LOOKAHEAD] = org.openshouter.calendar.CalendarShout.clampMinutes(minutes)
    }

    suspend fun setBluetooth(connect: Boolean, battery: Boolean) = context.osDataStore.edit {
        it[SettingsSprint15.BT_CONN] = connect
        it[SettingsSprint15.BT_BATT] = battery
    }
}
