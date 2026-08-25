package org.openshouter.domain

import java.time.ZonedDateTime
import java.util.Locale

/** US/NATO-style spoken time for TTS (Zulu clock). */
object MilitaryTime {
    private val ONES = arrayOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen",
    )
    private val TENS = arrayOf("", "", "twenty", "thirty", "forty", "fifty")

    fun speak(hour: Int, minute: Int): String {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        return "${hourPart(h)} ${minutePart(m)}"
    }

    fun speakAt(now: ZonedDateTime): String = speak(now.hour, now.minute)

    private fun hourPart(hour: Int): String = when (hour) {
        0 -> "zero zero"
        in 1..9 -> "zero ${ONES[hour]}"
        else -> belowHundred(hour)
    }

    private fun minutePart(minute: Int): String = when (minute) {
        0 -> "hundred"
        in 1..9 -> "zero ${ONES[minute]}"
        else -> belowHundred(minute)
    }

    private fun belowHundred(value: Int): String = when {
        value < 20 -> ONES[value]
        value % 10 == 0 -> TENS[value / 10]
        else -> "${TENS[value / 10]}-${ONES[value % 10]}"
    }
}
