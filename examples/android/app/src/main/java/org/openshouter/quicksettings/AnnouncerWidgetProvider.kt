package org.openshouter.quicksettings

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dagger.hilt.android.EntryPointAccessors
import dev.foss.goldenpath.R
import kotlinx.coroutines.runBlocking
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.service.OpenShouterRuntime

class AnnouncerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val enabled = runBlocking {
            EntryPointAccessors.fromApplication(context, OpenShouterEntryPoint::class.java)
                .settings().snapshot().announcerEnabled
        }
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_announcer)
            views.setTextViewText(
                R.id.widget_label,
                context.getString(if (enabled) R.string.widget_on else R.string.widget_off),
            )
            val intent = Intent(context, AnnouncerWidgetProvider::class.java).setAction(ACTION_TOGGLE)
            views.setOnClickPendingIntent(
                R.id.widget_label,
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE),
            )
            manager.updateAppWidget(id, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_TOGGLE) return
        runBlocking {
            val settings = EntryPointAccessors.fromApplication(context, OpenShouterEntryPoint::class.java)
                .settings()
            settings.setEnabled(!settings.snapshot().announcerEnabled)
        }
        OpenShouterRuntime.ensureStarted(context)
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(
            android.content.ComponentName(context, AnnouncerWidgetProvider::class.java),
        )
        onUpdate(context, mgr, ids)
    }

    companion object {
        const val ACTION_TOGGLE = "org.openshouter.action.TOGGLE"
    }
}
