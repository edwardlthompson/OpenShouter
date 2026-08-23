package org.openshouter.domain

import java.util.Locale

object TtsLocaleMenu {
    fun languageCode(tag: String): String {
        val raw = tag.trim()
        if (raw.isEmpty()) return ""
        val fromLocale = Locale.forLanguageTag(raw).language
        return fromLocale.ifBlank { raw.substringBefore('-') }.lowercase(Locale.ROOT)
    }

    fun languages(tags: List<String>, ui: Locale = Locale.US): List<String> =
        tags.map { languageCode(it) }.filter { it.isNotEmpty() }.distinct()
            .sortedBy { displayLanguage(it, ui).lowercase(ui) }

    fun accents(tags: List<String>, language: String, ui: Locale = Locale.US): List<String> {
        val want = language.lowercase(Locale.ROOT)
        if (want.isEmpty()) return emptyList()
        return tags.filter { languageCode(it) == want }.distinct()
            .sortedBy { displayAccent(it, ui).lowercase(ui) }
    }

    fun voicesFor(voices: List<TtsVoiceCandidate>, tag: String): List<TtsVoiceCandidate> {
        val want = tag.trim()
        if (want.isEmpty()) return emptyList()
        val exact = voices.filter { it.languageTag.equals(want, ignoreCase = true) }
        val pool = exact.ifEmpty {
            voices.filter { languageCode(it.languageTag) == languageCode(want) }
        }
        return pool.sortedWith(
            compareBy<TtsVoiceCandidate> { it.networkRequired }
                .thenByDescending { it.quality }
                .thenBy { it.name },
        )
    }

    fun displayLanguage(code: String, ui: Locale = Locale.US): String {
        val raw = code.trim()
        if (raw.isEmpty()) return ""
        return Locale.forLanguageTag(raw).getDisplayLanguage(ui).ifBlank { raw }
    }

    fun displayAccent(tag: String, ui: Locale = Locale.US): String {
        val loc = Locale.forLanguageTag(tag.trim())
        val country = loc.getDisplayCountry(ui)
        val script = loc.getDisplayScript(ui)
        return listOf(country, script).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { tag }
    }

    fun shortName(name: String): String {
        val parts = name.split('-').filter { it.isNotEmpty() }
        return if (parts.size <= 2) name else parts.takeLast(2).joinToString("-")
    }
}
