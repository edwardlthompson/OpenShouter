package org.openshouter.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.openshouter.alarm.AlarmScheduler
import org.openshouter.data.ReminderEntity
import org.openshouter.domain.ReminderContract

object ReminderAlarms {
    fun sync(context: Context, scheduler: AlarmScheduler, reminders: List<ReminderEntity>, exact: Boolean) {
        reminders.forEach { row ->
            val op = pending(context, row)
            if (!row.enabled || !ReminderContract.validId(row.id)) {
                scheduler.cancel(op)
            } else {
                scheduler.schedule(row.nextAtMillis, op, exact = exact)
            }
        }
    }

    fun cancel(context: Context, scheduler: AlarmScheduler, id: Long) {
        scheduler.cancel(pending(context, ReminderEntity(id = id, text = "x", intervalMinutes = 60, nextAtMillis = 0, enabled = false, alsoNotify = false)))
    }

    private fun pending(context: Context, row: ReminderEntity): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderContract.ACTION
            putExtra(ReminderContract.EXTRA_ID, row.id)
            putExtra(ReminderContract.EXTRA_ALSO_NOTIFY, row.alsoNotify)
        }
        return PendingIntent.getBroadcast(
            context,
            ReminderContract.requestCode(row.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
