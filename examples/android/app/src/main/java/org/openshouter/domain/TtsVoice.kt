package org.openshouter.domain

data class TtsVoice(
    val pitch: Float = DEFAULT_PITCH,
    val languageTag: String = "",
    val engine: String = "",
    val voiceName: String = "",
    val minQuality: Int = QUALITY_VERY_HIGH,
) {
    fun clamp(): TtsVoice = copy(
        pitch = pitch.coerceIn(MIN_PITCH, MAX_PITCH),
        languageTag = languageTag.trim().take(MAX_TAG),
        engine = sanitizeId(engine, MAX_ENGINE),
        voiceName = sanitizeId(voiceName, MAX_VOICE_NAME),
        minQuality = minQuality.coerceIn(QUALITY_AUTO, QUALITY_VERY_HIGH),
    )

    companion object {
        const val DEFAULT_PITCH = 1.0f
        const val MIN_PITCH = 0.5f
        const val MAX_PITCH = 2.0f
        const val MAX_TAG = 35
        const val MAX_ENGINE = 80
        const val MAX_VOICE_NAME = 80
        const val QUALITY_AUTO = 0
        const val QUALITY_NORMAL = 300
        const val QUALITY_HIGH = 400
        const val QUALITY_VERY_HIGH = 500

        fun sanitizeId(raw: String, max: Int): String =
            raw.trim().filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }.take(max)
    }
}
