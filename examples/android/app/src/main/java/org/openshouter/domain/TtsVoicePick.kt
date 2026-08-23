package org.openshouter.domain

data class TtsVoiceCandidate(
    val name: String,
    val languageTag: String,
    val quality: Int,
    val latency: Int,
    val networkRequired: Boolean,
)

object TtsVoicePick {
    fun best(
        candidates: List<TtsVoiceCandidate>,
        preferredTag: String,
        minQuality: Int = 0,
        preferredName: String = "",
    ): TtsVoiceCandidate? {
        if (candidates.isEmpty()) return null
        val named = preferredName.trim()
        val usable = if (minQuality > 0) candidates.filter { it.quality >= minQuality } else candidates
        if (usable.isEmpty()) return null
        if (named.isNotEmpty()) {
            return usable.firstOrNull { it.name == named }
        }
        val pref = preferredTag.trim()
        val localeMatch = if (pref.isEmpty()) {
            usable
        } else {
            usable.filter { matchesLocale(it.languageTag, pref) }.ifEmpty { usable }
        }
        val local = localeMatch.filterNot { it.networkRequired }.ifEmpty { localeMatch }
        return local.maxWithOrNull(
            compareBy<TtsVoiceCandidate> { it.quality }
                .thenBy { -it.latency }
                .thenBy { it.name },
        )
    }

    private fun matchesLocale(tag: String, preferred: String): Boolean {
        val have = tag.trim()
        val want = preferred.trim()
        if (have.isEmpty() || want.isEmpty()) return false
        return have.equals(want, ignoreCase = true) ||
            have.startsWith("$want-", ignoreCase = true) ||
            want.startsWith("$have-", ignoreCase = true)
    }
}
