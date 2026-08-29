package org.openshouter.silence

import android.app.Notification
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.openshouter.data.SoundLeakDao
import org.openshouter.data.SoundLeakEntity

object SoundLeakWatch {
    fun inspect(
        context: Context,
        sbn: StatusBarNotification,
        ranking: NotificationListenerService.Ranking?,
    ): SoundInspect {
        val channel = ranking?.channel
        return SoundInspect(
            packageName = sbn.packageName,
            channelId = sbn.notification.channelId.orEmpty(),
            channelName = channel?.name?.toString().orEmpty(),
            channelSound = channel?.sound?.toString(),
            notificationSound = sbn.notification.sound?.toString(),
            usesDefaultSound = sbn.notification.defaults and Notification.DEFAULT_SOUND != 0,
            importance = channel?.importance ?: 3,
            silentFlag = Build.VERSION.SDK_INT >= 29 &&
                (sbn.notification.flags and SoundLeakPolicy.FLAG_SILENT != 0),
            isGroup = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
            defaultNotificationSilent = SystemDefaultSound.notificationIsSilent(context),
        )
    }

    suspend fun apply(dao: SoundLeakDao, inspect: SoundInspect, now: Long = System.currentTimeMillis()) {
        if (inspect.defaultNotificationSilent) dao.deleteByEvidence(SoundEvidence.DEFAULT_SOUND.name)
        when (SoundLeakPolicy.action(inspect)) {
            SoundLeakAction.UPSERT -> {
                val evidence = SoundLeakPolicy.evidence(inspect) ?: return
                dao.upsert(
                    SoundLeakEntity(
                        inspect.packageName,
                        inspect.channelId,
                        inspect.channelName,
                        evidence.name,
                        now,
                    ),
                )
                dao.pruneTo(50)
            }
            SoundLeakAction.CLEAR -> dao.delete(inspect.packageName, inspect.channelId)
            SoundLeakAction.IGNORE -> Unit
        }
    }
}
