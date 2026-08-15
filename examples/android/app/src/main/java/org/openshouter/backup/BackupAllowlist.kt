package org.openshouter.backup

object BackupAllowlist {
    const val SETTINGS = "settings.json"
    const val APP_SPEAK = "app_speak_rules.json"
    val FILES = setOf(SETTINGS, APP_SPEAK)

    fun allowed(name: String): Boolean = name in FILES
}
