package org.openshouter.astro.alarm

import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import org.openshouter.astro.moon.LunarCalculator
import org.openshouter.astro.place.AstroPlace
import org.openshouter.astro.sun.SolarCalculator
import java.time.*

object AstroNextFire {

    fun nextInstant(alarm: AstroAlarm, place: AstroPlace?, now: Instant = Instant.now()): Instant? {
        if (!alarm.enabled) return null
        val zone = place?.zone ?: ZoneId.systemDefault()
        val nowZdt = ZonedDateTime.ofInstant(now, zone)

        return when (val target = alarm.target) {
            is AlarmTarget.CustomClock -> nextCustomClock(alarm, target, nowZdt)
            is AlarmTarget.Solar -> nextSolar(alarm, target, place, nowZdt, now)
            is AlarmTarget.Lunar -> nextLunar(alarm, target, place, nowZdt, now)
        }
    }

    private fun nextCustomClock(
        alarm: AstroAlarm,
        target: AlarmTarget.CustomClock,
        nowZdt: ZonedDateTime
    ): Instant? {
        val today = nowZdt.toLocalDate()
        for (i in 0..7) {
            val candidateDate = today.plusDays(i.toLong())
            if (alarm.daysOfWeek.isNotEmpty() && !alarm.daysOfWeek.contains(candidateDate.dayOfWeek)) {
                continue
            }
            val candidateZdt = candidateDate.atTime(target.hour, target.minute)
                .atZone(nowZdt.zone)
            val candidateInstant = candidateZdt.toInstant()
            if (candidateInstant.isAfter(nowZdt.toInstant())) {
                return candidateInstant
            }
        }
        return null
    }

    private fun nextSolar(
        alarm: AstroAlarm,
        target: AlarmTarget.Solar,
        place: AstroPlace?,
        nowZdt: ZonedDateTime,
        now: Instant
    ): Instant? {
        if (place == null || !place.isValid) return null
        val today = nowZdt.toLocalDate()
        for (i in 0..14) {
            val date = today.plusDays(i.toLong())
            if (alarm.daysOfWeek.isNotEmpty() && !alarm.daysOfWeek.contains(date.dayOfWeek)) {
                continue
            }
            val base = SolarCalculator.calculate(target.event, date, place.latitude, place.longitude, place.zone)
                ?: continue
            val fireInstant = base.plusSeconds(target.offsetMinutes * 60L)
            if (fireInstant.isAfter(now)) {
                return fireInstant
            }
        }
        return null
    }

    private fun nextLunar(
        alarm: AstroAlarm,
        target: AlarmTarget.Lunar,
        place: AstroPlace?,
        nowZdt: ZonedDateTime,
        now: Instant
    ): Instant? {
        if (place == null || !place.isValid) return null
        val today = nowZdt.toLocalDate()
        for (i in 0..31) {
            val date = today.plusDays(i.toLong())
            if (alarm.daysOfWeek.isNotEmpty() && !alarm.daysOfWeek.contains(date.dayOfWeek)) {
                continue
            }
            val base = LunarCalculator.calculate(target.event, date, place.latitude, place.longitude, place.zone)
                ?: continue
            val fireInstant = base.plusSeconds(target.offsetMinutes * 60L)
            if (fireInstant.isAfter(now)) {
                return fireInstant
            }
        }
        return null
    }
}
