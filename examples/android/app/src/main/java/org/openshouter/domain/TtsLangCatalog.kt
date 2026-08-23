package org.openshouter.domain

object TtsLangCatalog {
    private const val SHERPA =
        "ar-JO,bg,bn,ca-ES,cs-CZ,cy-GB,da-DK,de-DE,el-GR,en-GB,en-US,es-ES,es-MX," +
            "et,fa-IR,fi-FI,fr-FR,ga,hr,hu-HU,is-IS,it-IT,ka-GE,kk-KZ,lb-LU,lt,lv-LV," +
            "mt,ne-NP,nl-BE,no-NO,pl-PL,pt-BR,pt-PT,ro-RO,ru-RU,sk-SK,sl-SI,sr-RS," +
            "sv-SE,sw-CD,tr-TR,uk-UA,vi-VN,zh-CN"
    private const val RHVOICE =
        "sq,be,hr,cs,en-US,en-GB,eo,ka,ky,mk,ne,pl,pt-BR,ro,ru,sr,sk,tn,es,tt,tk,uk,uz,vi"
    private const val SHERPA_HIGH = "de-DE,en-GB,en-US,es-MX,kk-KZ"
    private const val SHERPA_LOW = "el-GR"

    fun sherpaTags(): List<String> = split(SHERPA)
    fun rhvoiceTags(): List<String> = split(RHVOICE)
    fun allTags(): List<String> = (sherpaTags() + rhvoiceTags()).distinct()

    fun merge(installed: List<String>): List<String> =
        (installed + allTags()).filter { it.isNotBlank() }.distinct()

    fun filterTags(
        tags: List<String>,
        voices: List<TtsVoiceCandidate>,
        minQuality: Int,
    ): List<String> = if (minQuality <= 0) tags else tags.filter { meets(it, voices, minQuality) }

    fun meets(tag: String, voices: List<TtsVoiceCandidate>, minQuality: Int): Boolean {
        if (minQuality <= 0 || tag.isBlank()) return true
        val installed = voices.filter { TtsLocaleMenu.languageCode(it.languageTag) == TtsLocaleMenu.languageCode(tag) ||
            it.languageTag.equals(tag, ignoreCase = true) }
            .maxOfOrNull { it.quality } ?: 0
        return maxOf(installed, catalogQuality(tag)) >= minQuality
    }

    fun catalogQuality(tag: String): Int {
        val want = tag.trim()
        if (want.isEmpty()) return 0
        if (split(SHERPA_HIGH).any { it.equals(want, ignoreCase = true) }) return TtsVoice.QUALITY_VERY_HIGH
        if (split(SHERPA_LOW).any { it.equals(want, ignoreCase = true) }) return TtsVoice.QUALITY_NORMAL
        if (covers(sherpaTags(), tag)) return TtsVoice.QUALITY_HIGH
        if (covers(rhvoiceTags(), tag)) return TtsVoice.QUALITY_NORMAL
        return 0
    }

    fun enginesFor(tag: String): List<String> = buildList {
        if (covers(sherpaTags(), tag)) add(TtsSourceCatalog.SHERPA)
        if (covers(rhvoiceTags(), tag)) add(TtsSourceCatalog.RHVOICE)
    }

    fun packInstalled(installed: List<String>, tag: String): Boolean = covers(installed, tag)

    fun keepOrPrefer(
        currentEngine: String,
        tag: String,
        installedTags: List<String>,
        installedPkgs: Set<String>,
        minQuality: Int,
    ): String {
        if (packInstalled(installedTags, tag)) return currentEngine
        val engines = enginesFor(tag)
        val preferSherpa = minQuality >= TtsVoice.QUALITY_HIGH && TtsSourceCatalog.SHERPA in engines
        val choice = if (preferSherpa) TtsSourceCatalog.SHERPA else engines.firstOrNull().orEmpty()
        return if (choice in installedPkgs) choice else currentEngine
    }

    fun covers(tags: List<String>, want: String): Boolean {
        val w = want.trim()
        if (w.isEmpty()) return false
        val lang = TtsLocaleMenu.languageCode(w)
        return tags.any { have ->
            have.equals(w, ignoreCase = true) ||
                TtsLocaleMenu.languageCode(have) == lang ||
                w.startsWith("$have-", ignoreCase = true) ||
                have.startsWith("$w-", ignoreCase = true)
        }
    }

    private fun split(raw: String): List<String> =
        raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}
