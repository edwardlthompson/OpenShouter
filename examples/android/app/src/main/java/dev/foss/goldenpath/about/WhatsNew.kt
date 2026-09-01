package dev.foss.goldenpath.about

object WhatsNew {
    fun shouldShow(lastSeenVersionCode: Int, currentVersionCode: Int): Boolean =
        lastSeenVersionCode in 1 until currentVersionCode

    fun highlightsForVersion(versionCode: Int): List<String> = when {
        versionCode >= 30 -> listOf(
            "AstroAlarm sun/moon clocks moved to a standalone app on GitHub",
            "Call dedup and call waiting announcements",
            "Custom time-shout intervals and calendar allowlist",
            "High contrast accessibility theme and German/Portuguese translations",
        )
        versionCode >= 29 -> listOf(
            "Call dedup and call waiting announcements",
            "Custom time-shout intervals and calendar allowlist",
            "High contrast accessibility theme and German/Portuguese translations",
        )
        else -> emptyList()
    }
}
