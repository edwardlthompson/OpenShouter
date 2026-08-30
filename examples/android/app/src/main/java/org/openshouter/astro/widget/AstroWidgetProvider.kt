package org.openshouter.astro.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.foss.goldenpath.MainActivity
import dev.foss.goldenpath.R
import org.openshouter.astro.alarm.AstroAlarmStore
import org.openshouter.astro.model.SolarEventType
import org.openshouter.astro.place.AstroPlaceStore
import org.openshouter.astro.sun.SolarCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AstroWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val placeStore = AstroPlaceStore(context)
        val alarmStore = AstroAlarmStore(context)
        val place = placeStore.get()
        val alarms = alarmStore.getAll()
        val now = Instant.now()

        val bitmap = AstroDiskRenderer.renderDisk(place, alarms, now)
        val zone = place?.zone ?: java.time.ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val nextRise = place?.let { SolarCalculator.calculate(SolarEventType.Sunrise, today, it.latitude, it.longitude, it.zone) }
        val nextSet = place?.let { SolarCalculator.calculate(SolarEventType.Sunset, today, it.latitude, it.longitude, it.zone) }

        val fmt = DateTimeFormatter.ofPattern("HH:mm").withZone(zone)
        val desc = buildString {
            append(context.getString(R.string.astro_widget_desc))
            if (nextRise != null && nextSet != null) {
                append(" - Sunrise: ").append(fmt.format(nextRise))
                append(", Sunset: ").append(fmt.format(nextSet))
            }
        }

        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            8830,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_astro).apply {
                setImageViewBitmap(R.id.widget_astro_disk, bitmap)
                setContentDescription(R.id.widget_astro_disk, desc)
                setOnClickPendingIntent(R.id.widget_astro_root, pendingIntent)
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_TIME_TICK ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED
        ) {
            val mgr = AppWidgetManager.getInstance(context) ?: return
            val ids = mgr.getAppWidgetIds(ComponentName(context, AstroWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                onUpdate(context, mgr, ids)
            }
        }
    }
}
