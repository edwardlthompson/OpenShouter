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
    const val LATE_GRACE_MS = 120_000L

    fun normalizeInterval(minutes: Int): Int = when {
        minutes <= 0 -> INTERVAL_HOUR
        minutes in 1..720 -> minutes
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

    fun currentSlotStartMillis(
        nowMillis: Long,
        intervalMinutes: Int,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Long {
        val interval = normalizeInterval(intervalMinutes).toLong()
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val startOfDay = now.toLocalDate().atStartOfDay(zone)
        val minuteOfDay = now.hour * 60L + now.minute
        val slot = (minuteOfDay / interval) * interval
        return startOfDay.plusMinutes(slot).toInstant().toEpochMilli()
    }

    fun isSlotAligned(
        nowMillis: Long,
        intervalMinutes: Int,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        val interval = normalizeInterval(intervalMinutes)
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        return (now.hour * 60 + now.minute) % interval == 0
    }

    fun shouldSpeakSlot(
        slotStartMillis: Long,
        lastSpokenSlotMillis: Long,
        nowMillis: Long,
        requireAligned: Boolean,
        intervalMinutes: Int,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        if (slotStartMillis == lastSpokenSlotMillis) return false
        if (requireAligned && !isSlotAligned(nowMillis, intervalMinutes, zone)) return false
        return nowMillis - slotStartMillis <= LATE_GRACE_MS
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

    fun formatClockForSpeech(
        now: ZonedDateTime,
        style: TimeHourStyle,
        system24Hour: Boolean,
        locale: Locale = Locale.getDefault(),
    ): String = if (use24Hour(style, system24Hour)) {
        MilitaryTime.speakAt(now)
    } else {
        formatClock(now, style, system24Hour, locale)
    }
}
