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
import org.openshouter.domain.AppOverride
import org.openshouter.domain.AppSpeakPolicy
import org.openshouter.domain.IgnoreReason
import org.openshouter.domain.NotificationPolicy
import org.openshouter.domain.RegexAction
import org.openshouter.domain.RegexFilter
import org.openshouter.domain.RegexRule
import org.openshouter.domain.SpokenEvent
import org.openshouter.message.MessageChannel
import org.openshouter.service.OpenShouterEntryPoint

class OpenShouterNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var lastKey: String? = null

    @Volatile
    private var lastAt: Long = 0L

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.isOngoing) return
        val ep = EntryPointAccessors.fromApplication(
            applicationContext,
            OpenShouterEntryPoint::class.java,
        )
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val ticker = extras.getCharSequence(EXTRA_TICKER_TEXT)?.toString().orEmpty()
        val sub = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val info = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString().orEmpty()
        val bigTitle = extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString().orEmpty()
        val bigSummary = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty()
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.joinToString(" ") { it.toString() }
            .orEmpty()
        val isGroupSummary = (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        val app = sbn.packageName
        val tokens = mapOf(
            "ticker" to ticker,
            "subtext" to sub,
            "bigtext" to big,
            "info" to info,
            "bigtitle" to bigTitle,
            "bigsummary" to bigSummary,
            "lines" to lines,
        )
        scope.launch {
            val settings = ep.settings().snapshot()
            if (!settings.notificationsEnabled) return@launch
            val people = extras.getCharSequence(Notification.EXTRA_PEOPLE)?.toString().orEmpty()
            val messaging = MessageChannel.isMessaging(app)
            val parsed = MessageChannel.parse(title, text, people)
            if (messaging) {
                val spoken = MessageChannel.utterance(settings, parsed, null) ?: return@launch
                if (!ep.gate().allow(settings, org.openshouter.domain.ShoutChannel.MESSAGE)) return@launch
                ep.tts().speak(
                    org.openshouter.domain.ChannelStates.spoken(
                        settings,
                        org.openshouter.domain.ShoutChannel.MESSAGE,
                        SpokenEvent.Kind.MESSAGE,
                        spoken,
                    ),
                )
                return@launch
            }
            val rule = AppSpeakPolicy.ruleFor(app, ep.appSpeak().snapshot())
            if (rule == null && !messaging) return@launch
            val now = System.currentTimeMillis()
            val key = NotificationPolicy.repeatKey(app, title, text)
            val reason = settings.notificationPolicy.decide(
                title, text, isGroupSummary, key, lastKey, lastAt, now,
            )
            if (reason != IgnoreReason.NONE) {
                ep.history().insert(
                    HistoryEntity(
                        postedAt = sbn.postTime,
                        packageName = app,
                        title = title,
                        text = text,
                        spoken = "",
                        ignoreReason = reason.name,
                    ),
                )
                ep.history().pruneTo(100)
                return@launch
            }
            lastKey = key
            lastAt = now
            val label = runCatching {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(app, 0)).toString()
            }.getOrDefault(app)
            val format = AppOverride(app, settings.appFormats[app]).mergeFormat(settings.ttsFormat)
            val spoken = NotificationUtterance.build(
                rule, app, settings.messageChannel.speakBody, format, label, title, text, tokens,
            )
            if (spoken.isBlank()) return@launch
            val rules = ep.regex().snapshot().map {
                RegexRule(
                    it.pattern,
                    runCatching { RegexAction.valueOf(it.action) }.getOrDefault(RegexAction.IGNORE),
                    it.replacement,
                )
            }
            val filtered = RegexFilter.apply(spoken, rules) ?: return@launch
            if (!ep.gate().allow(settings, org.openshouter.domain.ShoutChannel.NOTIFICATION)) return@launch
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
            ep.tts().speak(
                org.openshouter.domain.ChannelStates.spoken(
                    settings,
                    org.openshouter.domain.ShoutChannel.NOTIFICATION,
                    SpokenEvent.Kind.NOTIFICATION,
                    filtered,
                ),
            )
        }
    }

    private companion object {
        const val EXTRA_TICKER_TEXT = "android.tickerText"
    }
}
