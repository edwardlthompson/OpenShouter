package org.openshouter.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object TestNotification {
    const val CHANNEL_ID = "openshouter-test"

    fun post(context: Context) {
        runCatching {
            val mgr = context.getSystemService(NotificationManager::class.java) ?: return@runCatching
            mgr.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "OpenShouter",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
            mgr.notify(
                TEST_ID,
                Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                    .setContentTitle("OpenShouter")
                    .setContentText("Test notification")
                    .setAutoCancel(true)
                    .build(),
            )
        }
    }

    private const val TEST_ID = 42
}
