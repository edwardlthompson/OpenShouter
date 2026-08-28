package org.openshouter.domain

/** Maps the history dialog OpenShouter switch onto Apps-to-shout flags. */
object HistorySpeak {
    fun isShouting(packageName: String, rules: Map<String, AppSpeakRule>): Boolean =
        AppSpeakList.isSelected(packageName, rules)

    fun enabledFlags(enabled: Boolean): Pair<Boolean, Boolean> = enabled to enabled
}
