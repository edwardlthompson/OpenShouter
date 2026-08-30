package org.openshouter.domain

data class AppOverride(
    val packageName: String,
    val format: String? = null,
    val speakName: Boolean? = null,
    val speakBody: Boolean? = null,
    val ignoreEmpty: Boolean? = null,
    val ignoreGroup: Boolean? = null,
    val ignoreRepeats: Boolean? = null,
    val stream: TtsStream? = null,
    val delaySeconds: Int? = null,
    val maxLength: Int? = null,
    val minImportance: SpeakImportance? = null,
    val appNameCooldownSeconds: Int? = null,
) {
    fun mergeFormat(global: String): String = format?.trim()?.takeIf { it.isNotEmpty() } ?: global

    fun mergePlayback(global: TtsPlaybackPolicy): TtsPlaybackPolicy = global.copy(
        stream = stream ?: global.stream,
        delaySeconds = delaySeconds ?: global.delaySeconds,
        maxLength = maxLength ?: global.maxLength,
    ).clamp()

    fun mergeNotification(global: NotificationPolicy): NotificationPolicy = global.copy(
        ignoreEmpty = ignoreEmpty ?: global.ignoreEmpty,
        ignoreGroup = ignoreGroup ?: global.ignoreGroup,
        ignoreRepeats = ignoreRepeats ?: global.ignoreRepeats,
    )

    fun speakName(default: Boolean): Boolean = speakName ?: default

    fun speakBody(default: Boolean): Boolean = speakBody ?: default
}

object AppOverrides {
    fun parse(stored: Set<String>): Map<String, String> =
        parseFull(stored).mapValues { it.value.mergeFormat("") }.filterValues { it.isNotEmpty() }

    fun encode(map: Map<String, String>): Set<String> =
        map.filter { it.key.isNotBlank() && it.value.isNotBlank() }.map { "${it.key}=${it.value}" }.toSet()

    fun parseFull(stored: Set<String>): Map<String, AppOverride> = stored.mapNotNull { row ->
        if (row.contains('|')) parseRich(row) else parseLegacy(row)
    }.associateBy { it.packageName }

    fun encodeFull(map: Map<String, AppOverride>): Set<String> = map.values.mapNotNull { row ->
        val pkg = row.packageName.trim()
        if (pkg.isEmpty()) return@mapNotNull null
        val parts = mutableListOf(pkg)
        row.format?.trim()?.takeIf { it.isNotEmpty() }?.let { parts += "format=$it" }
        row.speakName?.let { parts += "name=${bit(it)}" }
        row.speakBody?.let { parts += "body=${bit(it)}" }
        row.ignoreEmpty?.let { parts += "empty=${bit(it)}" }
        row.ignoreGroup?.let { parts += "group=${bit(it)}" }
        row.ignoreRepeats?.let { parts += "repeat=${bit(it)}" }
        row.stream?.let { parts += "st=${it.name}" }
        row.delaySeconds?.let { parts += "delay=$it" }
        row.maxLength?.let { parts += "max=$it" }
        row.minImportance?.let { parts += "imp=${it.name}" }
        row.appNameCooldownSeconds?.let { parts += "cd=$it" }
        if (parts.size == 1) return@mapNotNull null
        parts.joinToString("|")
    }.toSet()

    private fun parseLegacy(row: String): AppOverride? {
        val idx = row.indexOf('=')
        if (idx <= 0) return null
        val pkg = row.substring(0, idx).trim()
        val format = row.substring(idx + 1).trim()
        if (pkg.isEmpty()) return null
        return AppOverride(pkg, format.takeIf { it.isNotEmpty() })
    }

    private fun parseRich(row: String): AppOverride? {
        val parts = row.split('|')
        val pkg = parts.first().trim()
        if (pkg.isEmpty()) return null
        val fields = parts.drop(1).mapNotNull { cell ->
            val idx = cell.indexOf('=')
            if (idx <= 0) null else cell.substring(0, idx) to cell.substring(idx + 1)
        }.toMap()
        return AppOverride(
            packageName = pkg,
            format = fields["format"]?.trim()?.takeIf { it.isNotEmpty() },
            speakName = bool(fields["name"]),
            speakBody = bool(fields["body"]),
            ignoreEmpty = bool(fields["empty"]),
            ignoreGroup = bool(fields["group"]),
            ignoreRepeats = bool(fields["repeat"]),
            stream = fields["st"]?.let { runCatching { TtsStream.valueOf(it) }.getOrNull() },
            delaySeconds = fields["delay"]?.toIntOrNull(),
            maxLength = fields["max"]?.toIntOrNull(),
            minImportance = fields["imp"]?.let { runCatching { SpeakImportance.valueOf(it) }.getOrNull() },
            appNameCooldownSeconds = fields["cd"]?.toIntOrNull(),
        )
    }

    private fun bool(raw: String?): Boolean? = when (raw) {
        "1" -> true
        "0" -> false
        else -> null
    }

    private fun bit(value: Boolean): String = if (value) "1" else "0"
}
