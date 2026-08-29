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
import org.openshouter.silence.SoundEvidence
import org.openshouter.silence.SoundLeakWatch
import org.openshouter.silence.SystemDefaultSound

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
        val ranking = rankingOf(sbn)
        val facts = NotificationFacts.from(sbn, nm, ranking?.channel?.name?.toString().orEmpty())
        val label = NotificationFacts.label(packageManager, facts.app)
        val priorityDnd = ep.audio().isPriorityDnd()
        scope.launch {
            recordLeak(sbn, ranking)
            NotificationPosted.handle(facts, ep, clock, label, priorityDnd, session)
        }
    }

    override fun onListenerConnected() {
        SoundLeakRescan.listener = this
        runCatching { entryPoint().audioSessions().start() }
    }

    override fun onListenerDisconnected() {
        if (SoundLeakRescan.listener === this) SoundLeakRescan.listener = null
    }

    fun rescanActive() {
        scope.launch {
            val dao = entryPoint().soundLeaks()
            if (SystemDefaultSound.notificationIsSilent(this@OpenShouterNotificationListener)) {
                dao.deleteByEvidence(SoundEvidence.DEFAULT_SOUND.name)
            }
            runCatching { activeNotifications }.getOrDefault(emptyArray()).forEach { sbn ->
                recordLeak(sbn, rankingOf(sbn))
            }
        }
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

    private fun rankingOf(sbn: StatusBarNotification): Ranking? = runCatching {
        val ranking = Ranking()
        ranking.takeIf { currentRanking.getRanking(sbn.key, it) }
    }.getOrNull()

    private suspend fun recordLeak(sbn: StatusBarNotification, ranking: Ranking?) {
        SoundLeakWatch.apply(entryPoint().soundLeaks(), SoundLeakWatch.inspect(this, sbn, ranking))
    }
}
