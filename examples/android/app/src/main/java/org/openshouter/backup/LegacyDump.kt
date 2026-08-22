package org.openshouter.backup

import org.openshouter.domain.AppSpeakRule

data class LegacyReminder(
    val text: String,
    val enabled: Boolean,
    val type: Int,
    val startMillis: Long,
)

data class LegacyDump(
    val rules: List<AppSpeakRule> = emptyList(),
    val quietCells: List<Pair<Int, Int>> = emptyList(),
    val reminders: List<LegacyReminder> = emptyList(),
    val nicks: Map<String, String> = emptyMap(),
    val blocked: Set<String> = emptySet(),
    val prefs: Map<String, String> = emptyMap(),
) {
    fun plus(other: LegacyDump) = LegacyDump(
        rules = rules + other.rules,
        quietCells = quietCells + other.quietCells,
        reminders = reminders + other.reminders,
        nicks = nicks + other.nicks,
        blocked = blocked + other.blocked,
        prefs = prefs + other.prefs,
    )

    val itemCount: Int
        get() = rules.size + reminders.size + nicks.size + blocked.size +
            (if (quietCells.isNotEmpty()) 1 else 0) + (if (prefs.isNotEmpty()) 1 else 0)
}

object ShouterLegacyParse {
    private val PKG = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+$")
    private val ON = setOf("1", "true", "yes")

    fun isSqlite(bytes: ByteArray): Boolean =
        bytes.size >= 16 && bytes.decodeToString(0, 15) == "SQLite format 3"

    fun shoutEnabled(raw: String): Boolean = raw.trim().lowercase() in ON

    fun validPackage(name: String): Boolean = name.length in 3..255 && PKG.matches(name)

    fun rulesFromRows(rows: List<Pair<String, String>>): List<AppSpeakRule> =
        rows.mapNotNull { (pkg, shout) ->
            val name = pkg.trim()
            if (!validPackage(name) || !shoutEnabled(shout)) null
            else AppSpeakRule(name, speakAppName = true, speakNotification = true)
        }

    fun prefsFromXml(bytes: ByteArray): Map<String, String>? {
        val text = runCatching { bytes.decodeToString() }.getOrNull() ?: return null
        if (!text.contains("name=\"")) return null
        val out = linkedMapOf<String, String>()
        val tagged = Regex(
            """<(boolean|string|int|long)\s+name="([^"]+)"(?:\s+value="([^"]*)")?\s*(?:/>|>([^<]*)</string>)""",
        )
        tagged.findAll(text).forEach { m ->
            val key = m.groupValues[2]
            if (key.startsWith("pkdbserprx")) return@forEach
            out[key] = m.groupValues[3].ifEmpty { m.groupValues[4] }
        }
        return out.takeIf { it.isNotEmpty() }
    }

    fun digits(raw: String?): List<String> =
        raw.orEmpty().split(',', ';', ' ')
            .map { it.filter(Char::isDigit).takeLast(10) }
            .filter { it.length >= 7 }
}
