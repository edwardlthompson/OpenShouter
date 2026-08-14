package org.openshouter.time

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.foss.goldenpath.MainActivity
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.domain.AppSettings
import org.openshouter.domain.TimeShout

@Singleton
class TimeShoutScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun sync(settings: AppSettings) {
        if (!settings.announcerEnabled || !settings.timeShoutEnabled) {
            cancel()
            return
        }
        val trigger = TimeShout.nextTriggerMillis(
            System.currentTimeMillis(),
            settings.timeShoutIntervalMinutes,
        )
        val alarm = pendingAlarm()
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching {
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                val show = PendingIntent.getActivity(
                    context,
                    REQ,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                am.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, show), alarm)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, alarm)
            }
        }
    }

    fun cancel() {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendingAlarm())
    }

    private fun pendingAlarm(): PendingIntent {
        val intent = Intent(context, TimeShoutReceiver::class.java).setAction(TimeShoutReceiver.ACTION)
        return PendingIntent.getBroadcast(
            context,
            REQ,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val REQ = 77
    }
}
