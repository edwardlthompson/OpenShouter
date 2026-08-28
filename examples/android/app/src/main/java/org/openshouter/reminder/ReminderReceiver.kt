package org.openshouter.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openshouter.alarm.AlarmScheduler
import org.openshouter.data.HistoryDao
import org.openshouter.data.ReminderDao
import org.openshouter.data.SettingsRepository
import org.openshouter.data.ShoutHistoryStore
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ReminderContract
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.service.OpenShouterRuntime
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var tts: TtsController
    @Inject lateinit var gate: SpeakGate
    @Inject lateinit var reminders: ReminderDao
    @Inject lateinit var scheduler: AlarmScheduler
    @Inject lateinit var history: HistoryDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderContract.ACTION) return
        val id = intent.getLongExtra(ReminderContract.EXTRA_ID, 0L)
        if (!ReminderContract.validId(id)) return
        val alsoNotify = intent.getBooleanExtra(ReminderContract.EXTRA_ALSO_NOTIFY, false)
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                OpenShouterRuntime.ensureStarted(app)
                val snap = settings.snapshot()
                val row = reminders.enabled().firstOrNull { it.id == id } ?: return@launch
                if (!gate.allow(snap, ShoutChannel.REMINDER)) return@launch
                ShoutHistoryStore.insertOnce(history, SpokenEvent.Kind.REMINDER, row.text)
                tts.speak(ChannelStates.spoken(snap, ShoutChannel.REMINDER, SpokenEvent.Kind.REMINDER, row.text))
                if (alsoNotify || row.alsoNotify) postNotice(app)
                val next = row.copy(
                    intervalMinutes = org.openshouter.domain.ReminderInterval.normalize(row.intervalMinutes),
                    nextAtMillis = org.openshouter.domain.ReminderInterval.nextAt(
                        System.currentTimeMillis(),
                        row.intervalMinutes,
                    ),
                )
                ReminderAlarms.sync(app, scheduler, listOf(next), snap.timeShoutExact)
            } finally {
                pending.finish()
            }
        }
    }

    private fun postNotice(context: Context) {
        runCatching {
            val mgr = context.getSystemService(NotificationManager::class.java) ?: return@runCatching
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL, "Reminders", NotificationManager.IMPORTANCE_DEFAULT),
            )
            mgr.notify(
                NOTICE_ID,
                Notification.Builder(context, CHANNEL)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("Reminder")
                    .setContentText("Voice reminder")
                    .setAutoCancel(true)
                    .build(),
            )
        }
    }

    private companion object {
        const val CHANNEL = "openshouter-reminder"
        const val NOTICE_ID = 71
    }
}
