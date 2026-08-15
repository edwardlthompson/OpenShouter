package org.openshouter.notification

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
    ): String {
        val effective = rule ?: AppSpeakRule(app, speakAppName = true, speakNotification = speakBody)
        if (!effective.active) return ""
        if (effective.speakAppName && !effective.speakNotification) return label.trim()
        val spokenApp = if (effective.speakAppName) label else ""
        return TtsFormat.notification(format, spokenApp, title, text, extras)
    }
}
