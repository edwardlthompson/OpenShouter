package org.openshouter.astro

import org.junit.Assert.*
import org.junit.Test
import org.openshouter.astro.alarm.AstroNextFire
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import org.openshouter.astro.model.SolarEventType
import org.openshouter.astro.place.AstroPlace
import java.time.*

class AstroNextFireTest {

    private val zone = ZoneId.of("America/New_York")
    private val samplePlace = AstroPlace("New York", 40.7128, -74.0060, zone.id)

    @Test
    fun testCustomClockSameDayFuture() {
        val date = LocalDate.of(2026, 8, 30)
        val now = ZonedDateTime.of(date, LocalTime.of(6, 0), zone).toInstant()

        val alarm = AstroAlarm(
            id = "1",
            label = "Morning",
            target = AlarmTarget.CustomClock(7, 30),
            daysOfWeek = emptySet() // Once
        )

        val next = AstroNextFire.nextInstant(alarm, place = null, now = now)
        assertNotNull(next)
        val nextZdt = ZonedDateTime.ofInstant(next, zone)
        assertEquals(7, nextZdt.hour)
        assertEquals(30, nextZdt.minute)
        assertEquals(date, nextZdt.toLocalDate())
    }

    @Test
    fun testCustomClockNextDayWhenPast() {
        val date = LocalDate.of(2026, 8, 30)
        val now = ZonedDateTime.of(date, LocalTime.of(8, 0), zone).toInstant()

        val alarm = AstroAlarm(
            id = "2",
            label = "Morning",
            target = AlarmTarget.CustomClock(7, 30),
            daysOfWeek = emptySet()
        )

        val next = AstroNextFire.nextInstant(alarm, place = null, now = now)
        assertNotNull(next)
        val nextZdt = ZonedDateTime.ofInstant(next, zone)
        assertEquals(7, nextZdt.hour)
        assertEquals(30, nextZdt.minute)
        assertEquals(date.plusDays(1), nextZdt.toLocalDate())
    }

    @Test
    fun testCustomClockWeekdayFilter() {
        // 2026-08-30 is a Sunday
        val date = LocalDate.of(2026, 8, 30)
        val now = ZonedDateTime.of(date, LocalTime.of(6, 0), zone).toInstant()

        val alarm = AstroAlarm(
            id = "3",
            label = "Weekday Alarm",
            target = AlarmTarget.CustomClock(7, 0),
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )

        val next = AstroNextFire.nextInstant(alarm, place = null, now = now)
        assertNotNull(next)
        val nextZdt = ZonedDateTime.ofInstant(next, zone)
        assertEquals(DayOfWeek.MONDAY, nextZdt.dayOfWeek)
        assertEquals(date.plusDays(1), nextZdt.toLocalDate())
    }

    @Test
    fun testSolarSunsetWithOffset() {
        val date = LocalDate.of(2026, 8, 30)
        val now = ZonedDateTime.of(date, LocalTime.of(12, 0), zone).toInstant()

        val alarm = AstroAlarm(
            id = "4",
            label = "Sunset -15m",
            target = AlarmTarget.Solar(SolarEventType.Sunset, offsetMinutes = -15),
            daysOfWeek = setOf(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)
        )

        val next = AstroNextFire.nextInstant(alarm, place = samplePlace, now = now)
        assertNotNull(next)
        assertTrue(next!!.isAfter(now))
    }

    @Test
    fun testSolarWithoutPlaceReturnsNull() {
        val now = Instant.now()
        val alarm = AstroAlarm(
            id = "5",
            label = "Sunrise",
            target = AlarmTarget.Solar(SolarEventType.Sunrise)
        )

        val next = AstroNextFire.nextInstant(alarm, place = null, now = now)
        assertNull(next)
    }
}
