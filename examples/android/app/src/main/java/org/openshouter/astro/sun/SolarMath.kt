package org.openshouter.astro.sun

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

object SolarMath {
    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val RAD_TO_DEG = 180.0 / Math.PI

    fun julianDay(date: LocalDate): Double {
        val y = date.year
        val m = date.monthValue
        val d = date.dayOfMonth
        val a = floor((14 - m) / 12.0)
        val yr = y + 4800 - a
        val mo = m + 12 * a - 3
        return d + floor((153 * mo + 2) / 5.0) + 365 * yr + floor(yr / 4.0) - floor(yr / 100.0) + floor(yr / 400.0) - 32045.0
    }

    fun julianCentury(jd: Double): Double = (jd - 2451545.0) / 36525.0

    fun geomMeanLongSun(t: Double): Double {
        var l0 = 280.46646 + t * (36000.76983 + 0.0003032 * t)
        while (l0 > 360.0) l0 -= 360.0
        while (l0 < 0.0) l0 += 360.0
        return l0
    }

    fun geomMeanAnomSun(t: Double): Double = 357.52911 + t * (35999.05029 - 0.0001537 * t)

    fun eccentEarthOrbit(t: Double): Double = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)

    fun sunEqOfCenter(t: Double, m: Double): Double {
        val mr = m * DEG_TO_RAD
        return sin(mr) * (1.914602 - t * (0.004817 + 0.000014 * t)) +
                sin(2 * mr) * (0.019993 - 0.000101 * t) + sin(3 * mr) * 0.000289
    }

    fun sunTrueLong(l0: Double, c: Double): Double = l0 + c

    fun sunApparentLong(t: Double, l0: Double, c: Double): Double {
        val o = sunTrueLong(l0, c)
        val omega = 125.04 - 1934.136 * t
        return o - 0.00569 - 0.00478 * sin(omega * DEG_TO_RAD)
    }

    fun meanObliqEcliptic(t: Double): Double {
        val sec = 21.448 - t * (46.8150 + t * (0.00059 - t * 0.001813))
        return 23.0 + (26.0 + (sec / 60.0)) / 60.0
    }

    fun obliqCorrection(t: Double): Double {
        val e0 = meanObliqEcliptic(t)
        val omega = 125.04 - 1934.136 * t
        return e0 + 0.00256 * cos(omega * DEG_TO_RAD)
    }

    fun sunDeclination(t: Double, l0: Double, c: Double): Double {
        val e = obliqCorrection(t) * DEG_TO_RAD
        val lambda = sunApparentLong(t, l0, c) * DEG_TO_RAD
        val sinT = sin(e) * sin(lambda)
        return asin(sinT) * RAD_TO_DEG
    }

    fun equationOfTime(t: Double, l0: Double, m: Double, e: Double): Double {
        val eps = obliqCorrection(t) * DEG_TO_RAD
        val l0r = l0 * DEG_TO_RAD
        val mr = m * DEG_TO_RAD
        val y = tan(eps / 2.0).pow(2)
        val sin2l0 = sin(2.0 * l0r)
        val sinm = sin(mr)
        val cos2l0 = cos(2.0 * l0r)
        val sin4l0 = sin(4.0 * l0r)
        val sin2m = sin(2.0 * mr)
        val eq = y * sin2l0 - 2.0 * e * sinm + 4.0 * e * y * sinm * cos2l0 -
                0.5 * y * y * sin4l0 - 1.25 * e * e * sin2m
        return eq * RAD_TO_DEG * 4.0 // in minutes of time
    }

    fun hourAngle(latDeg: Double, declinDeg: Double, zenithDeg: Double): Double? {
        val latR = latDeg * DEG_TO_RAD
        val decR = declinDeg * DEG_TO_RAD
        val zenR = zenithDeg * DEG_TO_RAD
        val cosH = (cos(zenR) - sin(latR) * sin(decR)) / (cos(latR) * cos(decR))
        if (cosH > 1.0 || cosH < -1.0) return null
        return acos(cosH) * RAD_TO_DEG
    }
}
