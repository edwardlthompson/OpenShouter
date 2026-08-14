package org.openshouter.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openshouter.data.HistoryEntity
import org.openshouter.domain.AppSpeakPolicy
import org.openshouter.domain.RegexAction
import org.openshouter.domain.RegexFilter
import org.openshouter.domain.RegexRule
import org.openshouter.domain.SpokenEvent
import org.openshouter.service.OpenShouterEntryPoint

class OpenShouterNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.isOngoing) return
        val ep = EntryPointAccessors.fromApplication(
            applicationContext,
            OpenShouterEntryPoint::class.java,
        )
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val app = sbn.packageName
        scope.launch {
            val settings = ep.settings().snapshot()
            if (!settings.notificationsEnabled) return@launch
            val rule = AppSpeakPolicy.ruleFor(app, ep.appSpeak().snapshot()) ?: return@launch
            val label = runCatching {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(app, 0)).toString()
            }.getOrDefault(app)
            val spoken = AppSpeakPolicy.utterance(rule, settings.ttsFormat, label, title, text)
            if (spoken.isBlank()) return@launch
            val rules = ep.regex().snapshot().map {
                RegexRule(
                    it.pattern,
                    runCatching { RegexAction.valueOf(it.action) }.getOrDefault(RegexAction.IGNORE),
                    it.replacement,
                )
            }
            val filtered = RegexFilter.apply(spoken, rules) ?: return@launch
            if (!ep.gate().allow(settings)) return@launch
            ep.history().insert(
                HistoryEntity(
                    postedAt = sbn.postTime,
                    packageName = app,
                    title = title,
                    text = text,
                    spoken = filtered,
                ),
            )
            ep.history().pruneTo(100)
            ep.tts().speak(SpokenEvent(SpokenEvent.Kind.NOTIFICATION, filtered))
        }
    }
}
