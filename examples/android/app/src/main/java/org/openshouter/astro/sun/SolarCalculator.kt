package org.openshouter.astro.sun

import org.openshouter.astro.model.SolarEventType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToLong

object SolarCalculator {
    const val ZENITH_OFFICIAL = 90.8333
    const val ZENITH_CIVIL = 96.0
    const val ZENITH_NAUTICAL = 102.0
    const val ZENITH_ASTRONOMICAL = 108.0
    const val ZENITH_GOLDEN_HOUR = 84.0
    const val ZENITH_BLUE_HOUR_BOTTOM = 98.0

    fun calculate(
        event: SolarEventType,
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ): Instant? {
        return when (event) {
            SolarEventType.Sunrise -> eventInstant(date, latitude, longitude, zoneId, ZENITH_OFFICIAL, isMorning = true)
            SolarEventType.Sunset -> eventInstant(date, latitude, longitude, zoneId, ZENITH_OFFICIAL, isMorning = false)
            SolarEventType.CivilDawn -> eventInstant(date, latitude, longitude, zoneId, ZENITH_CIVIL, isMorning = true)
            SolarEventType.CivilDusk -> eventInstant(date, latitude, longitude, zoneId, ZENITH_CIVIL, isMorning = false)
            SolarEventType.NauticalDawn -> eventInstant(date, latitude, longitude, zoneId, ZENITH_NAUTICAL, isMorning = true)
            SolarEventType.NauticalDusk -> eventInstant(date, latitude, longitude, zoneId, ZENITH_NAUTICAL, isMorning = false)
            SolarEventType.AstronomicalDawn -> eventInstant(date, latitude, longitude, zoneId, ZENITH_ASTRONOMICAL, isMorning = true)
            SolarEventType.AstronomicalDusk -> eventInstant(date, latitude, longitude, zoneId, ZENITH_ASTRONOMICAL, isMorning = false)
            SolarEventType.SolarNoon -> solarNoonInstant(date, latitude, longitude, zoneId)
            SolarEventType.SolarMidnight -> solarNoonInstant(date, latitude, longitude, zoneId)?.plusSeconds(12 * 3600)
            SolarEventType.GoldenHourMorning -> eventInstant(date, latitude, longitude, zoneId, ZENITH_GOLDEN_HOUR, isMorning = true)
            SolarEventType.GoldenHourEvening -> eventInstant(date, latitude, longitude, zoneId, ZENITH_GOLDEN_HOUR, isMorning = false)
            SolarEventType.BlueHourMorning -> eventInstant(date, latitude, longitude, zoneId, ZENITH_BLUE_HOUR_BOTTOM, isMorning = true)
            SolarEventType.BlueHourEvening -> eventInstant(date, latitude, longitude, zoneId, ZENITH_BLUE_HOUR_BOTTOM, isMorning = false)
            SolarEventType.MarchEquinox -> seasonInstant(date.year, 3, 20, 9, 0, zoneId)
            SolarEventType.JuneSolstice -> seasonInstant(date.year, 6, 21, 3, 0, zoneId)
            SolarEventType.SeptemberEquinox -> seasonInstant(date.year, 9, 22, 18, 0, zoneId)
            SolarEventType.DecemberSolstice -> seasonInstant(date.year, 12, 21, 15, 0, zoneId)
        }
    }

    fun solarNoonInstant(date: LocalDate, lat: Double, lon: Double, zone: ZoneId): Instant? {
        val jd = SolarMath.julianDay(date)
        val t = SolarMath.julianCentury(jd)
        val l0 = SolarMath.geomMeanLongSun(t)
        val m = SolarMath.geomMeanAnomSun(t)
        val e = SolarMath.eccentEarthOrbit(t)
        val eqTime = SolarMath.equationOfTime(t, l0, m, e)
        val offset = zone.rules.getOffset(date.atStartOfDay(zone).toInstant()).totalSeconds / 3600.0
        val noonMinutes = 720.0 - (4.0 * lon) - eqTime + (offset * 60.0)
        return minutesToInstant(date, zone, noonMinutes)
    }

    private fun eventInstant(
        date: LocalDate,
        lat: Double,
        lon: Double,
        zone: ZoneId,
        zenith: Double,
        isMorning: Boolean
    ): Instant? {
        val jd = SolarMath.julianDay(date)
        val t = SolarMath.julianCentury(jd)
        val l0 = SolarMath.geomMeanLongSun(t)
        val m = SolarMath.geomMeanAnomSun(t)
        val c = SolarMath.sunEqOfCenter(t, m)
        val e = SolarMath.eccentEarthOrbit(t)
        val decl = SolarMath.sunDeclination(t, l0, c)
        val ha = SolarMath.hourAngle(lat, decl, zenith) ?: return null
        val eqTime = SolarMath.equationOfTime(t, l0, m, e)
        val offset = zone.rules.getOffset(date.atStartOfDay(zone).toInstant()).totalSeconds / 3600.0
        val eventMinutes = if (isMorning) {
            720.0 - 4.0 * (lon + ha) - eqTime + (offset * 60.0)
        } else {
            720.0 - 4.0 * (lon - ha) - eqTime + (offset * 60.0)
        }
        return minutesToInstant(date, zone, eventMinutes)
    }

    private fun minutesToInstant(date: LocalDate, zone: ZoneId, minutesSinceMidnight: Double): Instant {
        val seconds = (minutesSinceMidnight * 60.0).roundToLong()
        val startOfDay = date.atStartOfDay(zone).toEpochSecond()
        return Instant.ofEpochSecond(startOfDay + seconds)
    }

    private fun seasonInstant(year: Int, month: Int, day: Int, hour: Int, min: Int, zone: ZoneId): Instant {
        return ZonedDateTime.of(year, month, day, hour, min, 0, 0, ZoneId.of("UTC"))
            .withZoneSameInstant(zone)
            .toInstant()
    }
}
