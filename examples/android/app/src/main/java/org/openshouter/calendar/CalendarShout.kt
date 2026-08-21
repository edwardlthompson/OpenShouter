package org.openshouter.calendar

object CalendarShout {
    const val MIN_MINUTES = 5
    const val DEFAULT_MINUTES = 15
    const val MAX_MINUTES = 60
    const val LOOK_AHEAD_MS = DEFAULT_MINUTES * 60L * 1000L
    val MINUTE_CHOICES = listOf(5, 15, 30)

    fun clampMinutes(minutes: Int): Int = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)

    fun lookAheadMs(minutes: Int): Long = clampMinutes(minutes) * 60L * 1000L

    fun phrase(title: String): String = title.trim()

    fun shouldSpeak(
        eventId: Long,
        begin: Long,
        now: Long,
        spoken: Pair<Long, Long>?,
        lookAheadMs: Long = LOOK_AHEAD_MS,
    ): Boolean {
        if (eventId <= 0L || begin <= 0L) return false
        if (begin < now || begin > now + lookAheadMs) return false
        return spoken != (eventId to begin)
    }
}
