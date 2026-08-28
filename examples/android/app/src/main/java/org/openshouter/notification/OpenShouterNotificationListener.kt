package org.openshouter.notification

import android.app.Notification
import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openshouter.call.CallNotification
import org.openshouter.service.OpenShouterEntryPoint

class OpenShouterNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val clock = RepeatClock()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (CallNotification.ignorePosted(
                sbn.packageName,
                sbn.isOngoing,
                sbn.notification.category == Notification.CATEGORY_CALL,
            )
        ) {
            return
        }
        val ep = EntryPointAccessors.fromApplication(
            applicationContext,
            OpenShouterEntryPoint::class.java,
        )
        val nm = getSystemService(NotificationManager::class.java)
        val channelName = channelNameFor(sbn)
        val facts = NotificationFacts.from(sbn, nm, channelName)
        val label = NotificationFacts.label(packageManager, facts.app)
        val priorityDnd = ep.audio().isPriorityDnd()
        scope.launch { NotificationPosted.handle(facts, ep, clock, label, priorityDnd) }
    }

    private fun channelNameFor(sbn: StatusBarNotification): String = runCatching {
        val ranking = Ranking()
        if (currentRanking.getRanking(sbn.key, ranking)) {
            ranking.channel?.name?.toString().orEmpty()
        } else {
            ""
        }
    }.getOrDefault("")
}
