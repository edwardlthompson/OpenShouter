package org.openshouter.astro.moon

import org.openshouter.astro.model.LunarEventType
import org.openshouter.astro.sun.SolarMath
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.*

object LunarCalculator {
    private const val SYNODIC_MONTH = 29.53058867
    private const val BASE_NEW_MOON_JD = 2451549.5

    fun moonAgeDays(date: LocalDate): Double {
        val jd = SolarMath.julianDay(date)
        val daysSinceBase = jd - BASE_NEW_MOON_JD
        var age = daysSinceBase % SYNODIC_MONTH
        if (age < 0) age += SYNODIC_MONTH
        return age
    }

    fun calculate(
        event: LunarEventType,
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ): Instant? {
        val baseStart = date.atStartOfDay(zoneId).toInstant()
        val age = moonAgeDays(date)
        return when (event) {
            LunarEventType.NewMoon -> nextPhaseInstant(date, zoneId, 0.0)
            LunarEventType.WaxingCrescent -> nextPhaseInstant(date, zoneId, 3.69)
            LunarEventType.FirstQuarter -> nextPhaseInstant(date, zoneId, 7.38)
            LunarEventType.WaxingGibbous -> nextPhaseInstant(date, zoneId, 11.07)
            LunarEventType.FullMoon -> nextPhaseInstant(date, zoneId, 14.765)
            LunarEventType.WaningGibbous -> nextPhaseInstant(date, zoneId, 18.45)
            LunarEventType.LastQuarter -> nextPhaseInstant(date, zoneId, 22.15)
            LunarEventType.WaningCrescent -> nextPhaseInstant(date, zoneId, 25.84)
            LunarEventType.MoonTransit -> moonTransitInstant(date, longitude, zoneId, age)
            LunarEventType.Moonrise -> moonRiseSetInstant(date, latitude, longitude, zoneId, age, isRise = true)
            LunarEventType.Moonset -> moonRiseSetInstant(date, latitude, longitude, zoneId, age, isRise = false)
        }
    }

    private fun nextPhaseInstant(date: LocalDate, zone: ZoneId, targetAge: Double): Instant {
        val currentAge = moonAgeDays(date)
        var diff = targetAge - currentAge
        if (diff < 0) diff += SYNODIC_MONTH
        val seconds = (diff * 86400.0).roundToLong()
        return date.atStartOfDay(zone).toInstant().plusSeconds(seconds)
    }

    private fun moonTransitInstant(date: LocalDate, lon: Double, zone: ZoneId, age: Double): Instant {
        val offsetHours = zone.rules.getOffset(date.atStartOfDay(zone).toInstant()).totalSeconds / 3600.0
        val transitHour = (age * (24.0 / SYNODIC_MONTH) + 12.0 - (lon / 15.0) + offsetHours) % 24.0
        val normHour = if (transitHour < 0) transitHour + 24.0 else transitHour
        return date.atStartOfDay(zone).toInstant().plusSeconds((normHour * 3600.0).roundToLong())
    }

    private fun moonRiseSetInstant(
        date: LocalDate,
        lat: Double,
        lon: Double,
        zone: ZoneId,
        age: Double,
        isRise: Boolean
    ): Instant {
        val transit = moonTransitInstant(date, lon, zone, age)
        val deltaHours = 6.0 + (sin(lat * Math.PI / 180.0) * 0.5)
        val shiftSeconds = (deltaHours * 3600.0).roundToLong()
        return if (isRise) transit.minusSeconds(shiftSeconds) else transit.plusSeconds(shiftSeconds)
    }
}
