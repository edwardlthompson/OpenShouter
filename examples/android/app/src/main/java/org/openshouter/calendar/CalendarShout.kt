package org.openshouter.calendar

object CalendarShout {
    const val LOOK_AHEAD_MS = 15L * 60L * 1000L

    fun phrase(title: String): String = title.trim()

    fun shouldSpeak(eventId: Long, begin: Long, now: Long, spoken: Pair<Long, Long>?): Boolean {
        if (eventId <= 0L || begin <= 0L) return false
        if (begin < now || begin > now + LOOK_AHEAD_MS) return false
        return spoken != (eventId to begin)
    }
}
