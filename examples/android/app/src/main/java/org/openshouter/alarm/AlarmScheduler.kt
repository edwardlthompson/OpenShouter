package org.openshouter.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.domain.AlarmPolicy

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule(
        triggerAtMillis: Long,
        operation: PendingIntent,
        exact: Boolean = false,
    ) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching {
            val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
            if (AlarmPolicy.useExact(exact, canExact)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            }
        }
    }

    fun cancel(operation: PendingIntent) {
        context.getSystemService(AlarmManager::class.java)?.cancel(operation)
    }
}
