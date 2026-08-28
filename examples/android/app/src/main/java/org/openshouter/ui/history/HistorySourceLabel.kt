package org.openshouter.ui.history

import dev.foss.goldenpath.R
import org.openshouter.domain.SpokenEvent

internal fun historySourceLabel(kind: String): Int? = when (kind) {
    SpokenEvent.Kind.TIME.name -> R.string.history_source_time
    SpokenEvent.Kind.POWER.name -> R.string.history_source_power
    SpokenEvent.Kind.REMINDER.name -> R.string.history_source_reminder
    SpokenEvent.Kind.CALENDAR.name -> R.string.history_source_calendar
    SpokenEvent.Kind.BLUETOOTH.name -> R.string.history_source_bluetooth
    SpokenEvent.Kind.GEO.name -> R.string.history_source_geo
    else -> null
}
