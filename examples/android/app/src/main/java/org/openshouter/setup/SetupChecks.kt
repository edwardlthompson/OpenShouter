package org.openshouter.setup

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import org.openshouter.notification.OpenShouterNotificationListener

object SetupChecks {
    fun listenerEnabled(context: Context): Boolean {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return false
        val name = ComponentName(context, OpenShouterNotificationListener::class.java)
        return if (Build.VERSION.SDK_INT >= 27) {
            nm.isNotificationListenerAccessGranted(name)
        } else {
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                ?.contains(context.packageName) == true
        }
    }

    fun granted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun notifyGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 || granted(context, Manifest.permission.POST_NOTIFICATIONS)

    fun batteryUnrestricted(context: Context): Boolean {
        val pm = context.getSystemService(PowerManager::class.java) ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun exactAlarmsAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        val am = context.getSystemService(AlarmManager::class.java) ?: return false
        return am.canScheduleExactAlarms()
    }

    fun openListener(context: Context) {
        launch(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    fun requestBatteryUnrestricted(context: Context) {
        val pkg = Uri.parse("package:${context.packageName}")
        val asked = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(pkg)
        if (!launch(context, asked)) {
            launch(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    fun openAppDetails(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:${context.packageName}"))
        launch(context, intent)
    }

    fun requestExactAlarms(context: Context) {
        if (Build.VERSION.SDK_INT < 31) return
        val pkg = Uri.parse("package:${context.packageName}")
        val targeted = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).setData(pkg)
        val list = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        if (!launch(context, targeted) && !launch(context, list)) openAppDetails(context)
    }

    private fun launch(context: Context, intent: Intent): Boolean {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }
}
