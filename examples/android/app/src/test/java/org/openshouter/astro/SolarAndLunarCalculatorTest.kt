package org.openshouter.astro

import org.junit.Assert.*
import org.junit.Test
import org.openshouter.astro.model.LunarEventType
import org.openshouter.astro.model.SolarEventType
import org.openshouter.astro.moon.LunarCalculator
import org.openshouter.astro.sun.SolarCalculator
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class SolarAndLunarCalculatorTest {

    @Test
    fun testLondonEquinoxSunriseSunset() {
        val date = LocalDate.of(2026, 3, 20)
        val lat = 51.5074
        val lon = -0.1278
        val zone = ZoneId.of("Europe/London")

        val sunrise = SolarCalculator.calculate(SolarEventType.Sunrise, date, lat, lon, zone)
        val sunset = SolarCalculator.calculate(SolarEventType.Sunset, date, lat, lon, zone)
        val noon = SolarCalculator.calculate(SolarEventType.SolarNoon, date, lat, lon, zone)

        assertNotNull("Sunrise should not be null", sunrise)
        assertNotNull("Sunset should not be null", sunset)
        assertNotNull("Solar noon should not be null", noon)

        val riseZdt = ZonedDateTime.ofInstant(sunrise, zone)
        val setZdt = ZonedDateTime.ofInstant(sunset, zone)
        val noonZdt = ZonedDateTime.ofInstant(noon, zone)

        assertEquals(6, riseZdt.hour)
        assertEquals(18, setZdt.hour)
        assertEquals(12, noonZdt.hour)
        assertTrue(sunrise!!.isBefore(sunset!!))
    }

    @Test
    fun testTwilightsOrder() {
        val date = LocalDate.of(2026, 6, 21)
        val lat = 40.7128
        val lon = -74.0060
        val zone = ZoneId.of("America/New_York")

        val astroDawn = SolarCalculator.calculate(SolarEventType.AstronomicalDawn, date, lat, lon, zone)
        val nautDawn = SolarCalculator.calculate(SolarEventType.NauticalDawn, date, lat, lon, zone)
        val civilDawn = SolarCalculator.calculate(SolarEventType.CivilDawn, date, lat, lon, zone)
        val sunrise = SolarCalculator.calculate(SolarEventType.Sunrise, date, lat, lon, zone)

        assertNotNull(astroDawn)
        assertNotNull(nautDawn)
        assertNotNull(civilDawn)
        assertNotNull(sunrise)

        assertTrue(astroDawn!!.isBefore(nautDawn!!))
        assertTrue(nautDawn.isBefore(civilDawn!!))
        assertTrue(civilDawn.isBefore(sunrise!!))
    }

    @Test
    fun testLunarPhasesCalculateValidInstants() {
        val date = LocalDate.of(2026, 8, 29)
        val lat = 35.6762
        val lon = 139.6503
        val zone = ZoneId.of("Asia/Tokyo")

        val fullMoon = LunarCalculator.calculate(LunarEventType.FullMoon, date, lat, lon, zone)
        val newMoon = LunarCalculator.calculate(LunarEventType.NewMoon, date, lat, lon, zone)
        val moonrise = LunarCalculator.calculate(LunarEventType.Moonrise, date, lat, lon, zone)

        assertNotNull(fullMoon)
        assertNotNull(newMoon)
        assertNotNull(moonrise)
    }
}
