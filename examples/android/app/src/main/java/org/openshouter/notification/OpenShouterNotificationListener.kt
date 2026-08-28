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
import org.openshouter.call.CallAnnounceAction
import org.openshouter.call.CallAnnounceSession
import org.openshouter.call.CallLoopGate
import org.openshouter.call.CallNotification
import org.openshouter.service.OpenShouterEntryPoint

class OpenShouterNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val clock = RepeatClock()
    private val session = CallAnnounceSession()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (CallNotification.ignorePosted(
                sbn.packageName,
                sbn.isOngoing,
                sbn.notification.category == Notification.CATEGORY_CALL,
            )
        ) {
            return
        }
        val ep = entryPoint()
        val nm = getSystemService(NotificationManager::class.java)
        val facts = NotificationFacts.from(sbn, nm, channelNameFor(sbn))
        val label = NotificationFacts.label(packageManager, facts.app)
        val priorityDnd = ep.audio().isPriorityDnd()
        scope.launch { NotificationPosted.handle(facts, ep, clock, label, priorityDnd, session) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        runCatching {
            if (session.onRemoved(sbn.packageName, sbn.key.orEmpty()) != CallAnnounceAction.INTERRUPT) {
                return
            }
            CallLoopGate.clear()
            entryPoint().tts().interrupt()
        }
    }

    private fun entryPoint(): OpenShouterEntryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        OpenShouterEntryPoint::class.java,
    )

    private fun channelNameFor(sbn: StatusBarNotification): String = runCatching {
        val ranking = Ranking()
        if (currentRanking.getRanking(sbn.key, ranking)) {
            ranking.channel?.name?.toString().orEmpty()
        } else {
            ""
        }
    }.getOrDefault("")
}
