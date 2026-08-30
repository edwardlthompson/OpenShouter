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
        val calendarName: String = "",
    )

    fun clampMinutes(minutes: Int): Int = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)

    fun lookAheadMs(minutes: Int): Long = clampMinutes(minutes) * 60L * 1000L

    fun phrase(title: String): String = title.trim()

    fun isAllowed(event: Event, allowlist: Set<String>): Boolean {
        if (allowlist.isEmpty()) return true
        if (event.calendarName.isBlank()) return true
        return allowlist.any { it.equals(event.calendarName, ignoreCase = true) }
    }

    fun morningBriefing(allDayEvents: List<Event>): String? {
        val titles = allDayEvents.filter { it.allDay && it.visible && !it.declined && phrase(it.title).isNotEmpty() }
            .map { phrase(it.title) }
            .distinct()
        if (titles.isEmpty()) return null
        return if (titles.size == 1) {
            "Today's all-day event: ${titles.first()}"
        } else {
            "Today's all-day events: ${titles.joinToString(", ")}"
        }
    }

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
        allowlist: Set<String> = emptySet(),
    ): Boolean {
        if (event.allDay || !event.visible || event.declined) return false
        if (!isAllowed(event, allowlist)) return false
        if (phrase(event.title).isEmpty()) return false
        return shouldSpeak(event.eventId, event.begin, now, spoken, lookAheadMs)
    }

    fun pickNext(
        events: List<Event>,
        now: Long,
        spoken: Pair<Long, Long>?,
        lookAheadMs: Long = LOOK_AHEAD_MS,
        allowlist: Set<String> = emptySet(),
    ): Event? = events.firstOrNull { eligible(it, now, spoken, lookAheadMs, allowlist) }
}
