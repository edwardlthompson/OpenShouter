package org.openshouter.calendar

object CalendarShout {
    const val MIN_MINUTES = 5
    const val DEFAULT_MINUTES = 15
    const val MAX_MINUTES = 60
    const val LOOK_AHEAD_MS = DEFAULT_MINUTES * 60L * 1000L
    val MINUTE_CHOICES = listOf(5, 15, 30)

    data class Event(
        val eventId: Long,
        val begin: Long,
        val title: String,
        val allDay: Boolean = false,
        val visible: Boolean = true,
        val declined: Boolean = false,
    )

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

    fun eligible(
        event: Event,
        now: Long,
        spoken: Pair<Long, Long>?,
        lookAheadMs: Long = LOOK_AHEAD_MS,
    ): Boolean {
        if (event.allDay || !event.visible || event.declined) return false
        if (phrase(event.title).isEmpty()) return false
        return shouldSpeak(event.eventId, event.begin, now, spoken, lookAheadMs)
    }

    fun pickNext(
        events: List<Event>,
        now: Long,
        spoken: Pair<Long, Long>?,
        lookAheadMs: Long = LOOK_AHEAD_MS,
    ): Event? = events.firstOrNull { eligible(it, now, spoken, lookAheadMs) }
}
