package org.openshouter.domain

data class TtsVoice(
    val pitch: Float = DEFAULT_PITCH,
    val languageTag: String = "",
) {
    fun clamp(): TtsVoice = copy(
        pitch = pitch.coerceIn(MIN_PITCH, MAX_PITCH),
        languageTag = languageTag.trim().take(MAX_TAG),
    )

    companion object {
        const val DEFAULT_PITCH = 1.0f
        const val MIN_PITCH = 0.5f
        const val MAX_PITCH = 2.0f
        const val MAX_TAG = 35
    }
}
