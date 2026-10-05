package org.openshouter.domain

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class SpokenClockTest {
    private val utc = ZoneOffset.UTC

    @Test
    fun naturalAndDigitAndMilitaryStyles() {
        val ninePm = OffsetDateTime.of(2026, 8, 15, 21, 0, 0, 0, utc).toZonedDateTime()
        val threeOhFive = OffsetDateTime.of(2026, 8, 15, 15, 5, 0, 0, utc).toZonedDateTime()
        val midnight = OffsetDateTime.of(2026, 8, 15, 0, 0, 0, 0, utc).toZonedDateTime()
        val noon = OffsetDateTime.of(2026, 8, 15, 12, 15, 0, 0, utc).toZonedDateTime()
        assertEquals(
            "nine o'clock",
            TimeShout.formatClockForSpeech(
                ninePm, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.NATURAL,
            ),
        )
        assertEquals(
            "nine o'clock PM",
            TimeShout.formatClockForSpeech(
                ninePm, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.NATURAL_AMPM,
            ),
        )
        assertEquals(
            "three oh five",
            TimeShout.formatClockForSpeech(
                threeOhFive, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.NATURAL,
            ),
        )
        assertEquals(
            "three oh five PM",
            TimeShout.formatClockForSpeech(
                threeOhFive, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.NATURAL_AMPM,
            ),
        )
        assertEquals(
            "twelve o'clock",
            TimeShout.formatClockForSpeech(
                midnight, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.NATURAL,
            ),
        )
        assertEquals(
            "twelve o'clock AM",
            TimeShout.formatClockForSpeech(
                midnight, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.NATURAL_AMPM,
            ),
        )
        assertEquals(
            "twelve fifteen",
            TimeShout.formatClockForSpeech(
                noon, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.NATURAL,
            ),
        )
        assertEquals(
            "9:00 PM",
            TimeShout.formatClockForSpeech(
                ninePm, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.DIGIT,
            ),
        )
        assertEquals(
            "21:00",
            TimeShout.formatClockForSpeech(
                ninePm, TimeHourStyle.HOUR_24, false, Locale.US, SpokenClockStyle.DIGIT,
            ),
        )
        assertEquals(
            "twenty-one hundred",
            TimeShout.formatClockForSpeech(
                ninePm, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.MILITARY,
            ),
        )
        assertEquals(
            "fifteen zero five",
            TimeShout.formatClockForSpeech(
                threeOhFive, TimeHourStyle.HOUR_12, true, Locale.US, SpokenClockStyle.MILITARY,
            ),
        )
        assertEquals(SpokenClockStyle.NATURAL, SpokenClockStyle.parse(null))
        assertEquals(SpokenClockStyle.DIGIT, SpokenClockStyle.parse("digit"))
        assertEquals(SpokenClockStyle.NATURAL, SpokenClockStyle.parse("nope"))
    }
}
