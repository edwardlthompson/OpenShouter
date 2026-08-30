package org.openshouter.domain

import java.util.Locale

enum class QuietProfile(val startMin: Int, val endMin: Int, val days: Set<Int>) {
    HOME(22 * 60, 7 * 60, (1..7).toSet()),
    WORK(9 * 60, 17 * 60, setOf(2, 3, 4, 5, 6)),
    WEEKEND(23 * 60, 9 * 60, setOf(1, 7)),
    CUSTOM(22 * 60, 7 * 60, (1..7).toSet());
}

object QuietHours {
    const val STEP_MINUTES = 15
    const val MINUTES_PER_DAY = 24 * 60
    val ALL_DAYS: Set<Int> = (1..7).toSet()

    fun profileFor(profile: QuietProfile): QuietHoursSchedule =
        QuietHoursSchedule(profile.startMin, profile.endMin, profile.days)

    fun clampMinutes(minutes: Int): Int {
        val wrapped = minutes % MINUTES_PER_DAY
        return if (wrapped < 0) wrapped + MINUTES_PER_DAY else wrapped
    }

    fun nudge(minutes: Int, deltaSteps: Int): Int =
        clampMinutes(minutes + deltaSteps * STEP_MINUTES)

    fun clockLabel(minutes: Int): String {
        val clamped = clampMinutes(minutes)
        val hour = clamped / 60
        val minute = clamped % 60
        val hour12 = (hour % 12).let { if (it == 0) 12 else it }
        val suffix = if (hour < 12) "AM" else "PM"
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, suffix)
    }

    fun windowLabel(start: Int, end: Int): String =
        "${clockLabel(start)}–${clockLabel(end)}"

    fun minutesUntil(fromMinute: Int, targetMinute: Int): Int {
        val diff = targetMinute - fromMinute
        return if (diff < 0) diff + MINUTES_PER_DAY else diff
    }

    fun toggleDay(days: Set<Int>, day: Int): Set<Int> {
        if (day !in ALL_DAYS) return days
        return if (day in days) {
            val next = days - day
            if (next.isEmpty()) days else next
        } else {
            days + day
        }
    }
}

data class QuietHoursSchedule(
    val startMinutes: Int,
    val endMinutes: Int,
    val days: Set<Int>,
)
