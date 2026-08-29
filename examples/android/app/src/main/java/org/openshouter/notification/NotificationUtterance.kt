package org.openshouter.notification

import org.openshouter.domain.AppNameCooldown
import org.openshouter.domain.AppSpeakRule
import org.openshouter.domain.TtsFormat

/** Local utterance helper — AppSpeakPolicy is outside this agent's scope. */
object NotificationUtterance {
    fun build(
        rule: AppSpeakRule?,
        app: String,
        speakBody: Boolean,
        format: String,
        label: String,
        title: String,
        text: String,
        extras: Map<String, String>,
        includeAppName: Boolean = true,
    ): String {
        val effective = rule ?: AppSpeakRule(app, speakAppName = true, speakNotification = speakBody)
        if (!effective.active) return ""
        val sayName = effective.speakAppName && includeAppName
        if (sayName && !effective.speakNotification) return label.trim()
        if (!sayName && !effective.speakNotification) return ""
        val spokenApp = if (sayName) label else ""
        val spokenTitle = if (!includeAppName && AppNameCooldown.isAppLabel(title, label)) {
            ""
        } else {
            title
        }
        if (spokenApp.isEmpty() && spokenTitle.isEmpty()) {
            return TtsFormat.notification("%text", "", "", text, extras)
        }
        return TtsFormat.notification(format, spokenApp, spokenTitle, text, extras)
    }
}
