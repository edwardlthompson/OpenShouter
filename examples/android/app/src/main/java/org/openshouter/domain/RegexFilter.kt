package org.openshouter.domain

enum class RegexAction { IGNORE, REPLACE }

data class RegexRule(
    val pattern: String,
    val action: RegexAction,
    val replacement: String = "",
)

object RegexFilter {
    const val MAX_PATTERN = 200

    fun apply(text: String, rules: List<RegexRule>): String? {
        var current = text
        for (rule in rules) {
            if (rule.pattern.isBlank() || rule.pattern.length > MAX_PATTERN) continue
            val regex = runCatching { Regex(rule.pattern, RegexOption.IGNORE_CASE) }.getOrNull()
                ?: continue
            when (rule.action) {
                RegexAction.IGNORE -> if (regex.containsMatchIn(current)) return null
                RegexAction.REPLACE -> current = regex.replace(current, rule.replacement)
            }
        }
        return current.trim().ifEmpty { null }
    }
}
