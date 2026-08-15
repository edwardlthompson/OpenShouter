package org.openshouter.domain

object ReminderInterval {
    const val HOUR = 60
    const val DAY = 24 * 60
    const val WEEK = 7 * 24 * 60
    const val MONTH = 30 * 24 * 60
    const val YEAR = 365 * 24 * 60

    val ALL = listOf(HOUR, DAY, WEEK, MONTH, YEAR)

    fun normalize(minutes: Int): Int =
        ALL.minByOrNull { kotlin.math.abs(it - minutes) } ?: HOUR

    fun nextAt(nowMillis: Long, minutes: Int): Long =
        nowMillis + normalize(minutes) * 60_000L
}
