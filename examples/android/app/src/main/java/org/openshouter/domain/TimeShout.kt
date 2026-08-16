package org.openshouter.domain

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class TimeHourStyle {
    SYSTEM,
    HOUR_12,
    HOUR_24,
    ;

    companion object {
        fun parse(raw: String?): TimeHourStyle =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: SYSTEM
    }
}

object TimeShout {
    const val INTERVAL_QUARTER = 15
    const val INTERVAL_HALF = 30
    const val INTERVAL_HOUR = 60

    fun normalizeInterval(minutes: Int): Int = when (minutes) {
        INTERVAL_QUARTER, INTERVAL_HALF, INTERVAL_HOUR -> minutes
        else -> INTERVAL_HOUR
    }

    fun nextTriggerMillis(
        nowMillis: Long,
        intervalMinutes: Int,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Long {
        val interval = normalizeInterval(intervalMinutes).toLong()
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val startOfDay = now.toLocalDate().atStartOfDay(zone)
        val minuteOfDay = now.hour * 60L + now.minute
        val slot = (minuteOfDay / interval) * interval
        return startOfDay.plusMinutes(slot + interval).toInstant().toEpochMilli()
    }

    fun use24Hour(style: TimeHourStyle, system24Hour: Boolean): Boolean = when (style) {
        TimeHourStyle.HOUR_24 -> true
        TimeHourStyle.HOUR_12 -> false
        TimeHourStyle.SYSTEM -> system24Hour
    }

    fun formatClock(
        now: ZonedDateTime,
        style: TimeHourStyle,
        system24Hour: Boolean,
        locale: Locale = Locale.getDefault(),
    ): String {
        val pattern = if (use24Hour(style, system24Hour)) "HH:mm" else "h:mm a"
        return now.format(DateTimeFormatter.ofPattern(pattern, locale))
    }
}
