package dev.foss.goldenpath.about

object WhatsNew {
    fun shouldShow(lastSeenVersionCode: Int, currentVersionCode: Int): Boolean =
        lastSeenVersionCode in 1 until currentVersionCode

    fun highlightsForVersion(versionCode: Int): List<String> = when {
        versionCode >= 29 -> listOf(
            "Sun & Moon astronomical alarms (Solar/Lunar event calculations)",
            "Call dedup and call waiting announcements",
            "Custom time-shout intervals and calendar allowlist",
            "High contrast accessibility theme and German/Portuguese translations",
        )
        else -> emptyList()
    }
}
