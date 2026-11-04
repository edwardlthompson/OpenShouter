package org.openshouter.domain

data class AppSpeakRule(
    val packageName: String,
    val speakAppName: Boolean,
    val speakNotification: Boolean,
) {
    val active: Boolean get() = speakAppName || speakNotification
}

object AppSpeakPolicy {
    fun ruleFor(packageName: String, rules: Map<String, AppSpeakRule>): AppSpeakRule? =
        rules[packageName]

    fun isMuted(rule: AppSpeakRule?): Boolean = rule != null && !rule.active

    fun allowsSpeak(rule: AppSpeakRule?): Boolean = rule?.active == true

    fun utterance(
        rule: AppSpeakRule,
        template: String,
        app: String,
        title: String,
        text: String,
    ): String {
        if (!rule.active) return ""
        if (rule.speakAppName && !rule.speakNotification) return app.trim()
        val spokenApp = if (rule.speakAppName) app else ""
        return TtsFormat.notification(template, spokenApp, title, text)
    }
}
