package dev.foss.goldenpath.settings

object SettingsLogic {
    val THEME_MODES = setOf("System", "Light", "Dark")

    fun isUpdateCheckEnabled(interval: String): Boolean = interval != "off"

    fun intervalForToggle(enabled: Boolean, current: String): String =
        when {
            !enabled -> "off"
            current == "off" -> "weekly"
            else -> current
        }

    fun themeModeName(raw: String, fallback: String = "System"): String =
        if (raw in THEME_MODES) raw else fallback
}
