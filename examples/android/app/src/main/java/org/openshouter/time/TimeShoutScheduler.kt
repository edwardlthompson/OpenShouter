package org.openshouter.time

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.alarm.AlarmScheduler
import org.openshouter.domain.AppSettings
import org.openshouter.domain.TimeShout

@Singleton
class TimeShoutScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarms: AlarmScheduler,
) {
    fun sync(settings: AppSettings) {
        if (!settings.announcerEnabled || !settings.timeShoutEnabled) {
            alarms.cancel(pendingAlarm())
            return
        }
        val trigger = TimeShout.nextTriggerMillis(
            System.currentTimeMillis(),
            settings.timeShoutIntervalMinutes,
        )
        alarms.schedule(trigger, pendingAlarm(), settings.timeShoutExact)
    }

    private fun pendingAlarm(): PendingIntent {
        val intent = Intent(context, TimeShoutReceiver::class.java)
            .setAction(TimeShoutReceiver.ACTION)
            .setPackage(context.packageName)
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
