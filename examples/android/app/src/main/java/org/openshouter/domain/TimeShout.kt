package org.openshouter.domain

import java.time.Instant
import java.time.ZoneId

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
}
