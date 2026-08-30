package org.openshouter.astro.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import androidx.core.app.NotificationCompat
import dev.foss.goldenpath.R
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import java.util.UUID

class AstroAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AstroAlarmScheduler.ACTION_ALARM_FIRE -> handleAlarmFire(context, intent)
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> AstroAlarmScheduler.rescheduleAll(context)
            AlarmClock.ACTION_SET_ALARM -> handleActionSetAlarm(context, intent)
            AlarmClock.ACTION_DISMISS_ALARM -> handleActionDismissAlarm(context)
        }
    }

    private fun handleAlarmFire(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(AstroAlarmScheduler.EXTRA_ALARM_ID) ?: ""
        val activityIntent = Intent(context, AstroAlarmActivity::class.java).apply {
            putExtra(AstroAlarmScheduler.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fullScreenPending = PendingIntent.getActivity(context, 8802, activityIntent, flags)

        ensureChannel(context)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_brand_mark)
            .setContentTitle(context.getString(R.string.astro_alarm_ringing))
            .setContentText(context.getString(R.string.astro_alarm_tap_to_open))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPending, true)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(NOTIFICATION_ID, notif)

        runCatching {
            context.startActivity(activityIntent)
        }
    }

    private fun handleActionSetAlarm(context: Context, intent: Intent) {
        val hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR, 7)
        val minute = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, 0)
        val message = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE) ?: context.getString(R.string.astro_custom_alarm_title)
        val alarm = AstroAlarm(
            id = UUID.randomUUID().toString(),
            label = message,
            target = AlarmTarget.CustomClock(hour, minute),
            enabled = true
        )
        AstroAlarmStore(context).save(alarm)
        AstroAlarmScheduler.rescheduleAll(context)
    }

    private fun handleActionDismissAlarm(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(NOTIFICATION_ID)
        AstroAlarmScheduler.rescheduleAll(context)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.astro_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.astro_channel_desc)
                setBypassDnd(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "os_astro_alarm_channel"
        const val NOTIFICATION_ID = 8800
    }
}
