package org.openshouter.astro.widget

import android.content.Context
import android.graphics.*
import org.openshouter.astro.alarm.AstroNextFire
import org.openshouter.astro.model.AstroAlarm
import org.openshouter.astro.model.SolarEventType
import org.openshouter.astro.place.AstroPlace
import org.openshouter.astro.sun.SolarCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.*

object AstroDiskRenderer {

    fun renderDisk(
        place: AstroPlace?,
        alarms: List<AstroAlarm>,
        now: Instant = Instant.now(),
        size: Int = 300
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        val radius = center - 8f

        val zone = place?.zone ?: java.time.ZoneId.systemDefault()
        val nowZdt = ZonedDateTime.ofInstant(now, zone)
        val nowMinutes = nowZdt.hour * 60 + nowZdt.minute + (nowZdt.second / 60f)
        val nowAngle = (nowMinutes / 1440f) * 360f

        val date = nowZdt.toLocalDate()
        val sunrise = place?.let { SolarCalculator.calculate(SolarEventType.Sunrise, date, it.latitude, it.longitude, it.zone) }
        val sunset = place?.let { SolarCalculator.calculate(SolarEventType.Sunset, date, it.latitude, it.longitude, it.zone) }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(center - radius, center - radius, center + radius, center + radius)

        // Draw day / night sectors
        if (sunrise != null && sunset != null) {
            val riseZdt = ZonedDateTime.ofInstant(sunrise, zone)
            val setZdt = ZonedDateTime.ofInstant(sunset, zone)
            val riseAngle = ((riseZdt.hour * 60 + riseZdt.minute) / 1440f) * 360f - nowAngle - 90f
            val setAngle = ((setZdt.hour * 60 + setZdt.minute) / 1440f) * 360f - nowAngle - 90f

            var sweepDay = (setAngle - riseAngle)
            if (sweepDay < 0) sweepDay += 360f

            paint.color = Color.DKGRAY
            canvas.drawCircle(center, center, radius, paint) // night

            paint.color = Color.WHITE
            canvas.drawArc(rect, riseAngle, sweepDay, true, paint) // day
        } else {
            paint.color = Color.DKGRAY
            canvas.drawCircle(center, center, radius, paint)
        }

        // Draw outer ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.GRAY
        canvas.drawCircle(center, center, radius, paint)

        // Fixed hand pointing straight up ("now")
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 3f
        paint.color = Color.RED
        canvas.drawLine(center, center, center, center - radius + 16f, paint)
        canvas.drawCircle(center, center, 6f, paint)

        // Red dots for alarms armed today
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        val todayAlarms = alarms.filter { it.enabled && (it.isOnce || it.daysOfWeek.contains(date.dayOfWeek)) }
        todayAlarms.forEach { alarm ->
            val next = AstroNextFire.nextInstant(alarm, place, now)
            if (next != null) {
                val alarmZdt = ZonedDateTime.ofInstant(next, zone)
                if (alarmZdt.toLocalDate() == date) {
                    val alarmMinutes = alarmZdt.hour * 60 + alarmZdt.minute
                    val alarmAngleDeg = ((alarmMinutes / 1440f) * 360f - nowAngle - 90f) * (Math.PI / 180.0)
                    val dotX = center + (radius - 20f) * cos(alarmAngleDeg).toFloat()
                    val dotY = center + (radius - 20f) * sin(alarmAngleDeg).toFloat()
                    canvas.drawCircle(dotX, dotY, 5f, paint)
                }
            }
        }

        return bitmap
    }
}
