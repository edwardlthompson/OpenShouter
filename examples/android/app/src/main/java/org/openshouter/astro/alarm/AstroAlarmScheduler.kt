package org.openshouter.astro.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.openshouter.astro.place.AstroPlaceStore
import java.time.Instant

object AstroAlarmScheduler {
    const val ACTION_ALARM_FIRE = "org.openshouter.astro.ACTION_ALARM_FIRE"
    const val EXTRA_ALARM_ID = "extra_astro_alarm_id"
    private const val REQUEST_CODE_ALARM = 8801

    fun rescheduleAll(context: Context) {
        val alarmStore = AstroAlarmStore(context)
        val placeStore = AstroPlaceStore(context)
        val place = placeStore.get()
        val alarms = alarmStore.getAll().filter { it.enabled }
        val now = Instant.now()

        val nextPairs = alarms.mapNotNull { alarm ->
            val instant = AstroNextFire.nextInstant(alarm, place, now) ?: return@mapNotNull null
            alarm to instant
        }.sortedBy { it.second }

        val earliest = nextPairs.firstOrNull()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        if (earliest == null) {
            cancelAlarm(context, alarmManager)
            return
        }

        val (alarm, instant) = earliest
        val fireIntent = Intent(context, AstroAlarmReceiver::class.java).apply {
            action = ACTION_ALARM_FIRE
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val operation = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM, fireIntent, flags)

        val showIntent = Intent(context, AstroAlarmActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val showOperation = PendingIntent.getActivity(context, REQUEST_CODE_ALARM + 1, showIntent, flags)

        val triggerEpochMs = instant.toEpochMilli()
        val clockInfo = AlarmManager.AlarmClockInfo(triggerEpochMs, showOperation)

        runCatching {
            alarmManager.setAlarmClock(clockInfo, operation)
        }
    }

    fun cancelAlarm(context: Context, alarmManager: AlarmManager? = null) {
        val am = alarmManager ?: (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager) ?: return
        val fireIntent = Intent(context, AstroAlarmReceiver::class.java).apply {
            action = ACTION_ALARM_FIRE
        }
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val operation = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM, fireIntent, flags)
        if (operation != null) {
            am.cancel(operation)
        }
    }
}
