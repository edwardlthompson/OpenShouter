package org.openshouter.domain

import java.time.ZonedDateTime

/** Natural 12-hour spoken clock for TTS (o'clock / oh N). */
object SpokenClock {
    private val ONES = arrayOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen",
    )
    private val TENS = arrayOf("", "", "twenty", "thirty", "forty", "fifty")

    fun natural(now: ZonedDateTime, amPm: Boolean): String =
        natural(now.hour, now.minute, amPm)

    fun natural(hour24: Int, minute: Int, amPm: Boolean): String {
        val h = hour24.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        val face = when {
            h == 0 || h == 12 -> 12
            h > 12 -> h - 12
            else -> h
        }
        val clock = when (m) {
            0 -> "${ONES[face]} o'clock"
            in 1..9 -> "${ONES[face]} oh ${ONES[m]}"
            else -> "${ONES[face]} ${belowHundred(m)}"
        }
        if (!amPm) return clock
        val meridiem = if (h < 12) "AM" else "PM"
        return "$clock $meridiem"
    }

    private fun belowHundred(value: Int): String = when {
        value < 20 -> ONES[value]
        value % 10 == 0 -> TENS[value / 10]
        else -> "${TENS[value / 10]}-${ONES[value % 10]}"
    }
}
