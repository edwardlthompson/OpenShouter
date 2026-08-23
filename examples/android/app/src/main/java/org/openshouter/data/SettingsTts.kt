package org.openshouter.data

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import org.openshouter.domain.TtsPlaybackPolicy
import org.openshouter.domain.TtsVoice

internal object SettingsTts {
    val ENGINE = stringPreferencesKey("tts_engine")
    val VOICE_NAME = stringPreferencesKey("tts_voice_name")
    val MIN_QUALITY = intPreferencesKey("tts_min_quality")

    fun voice(prefs: Preferences, pitch: Float, languageTag: String): TtsVoice = TtsVoice(
        pitch = pitch,
        languageTag = languageTag,
        engine = prefs[ENGINE].orEmpty(),
        voiceName = prefs[VOICE_NAME].orEmpty(),
        minQuality = prefs[MIN_QUALITY] ?: TtsVoice.QUALITY_VERY_HIGH,
    )

    fun write(prefs: MutablePreferences, policy: TtsPlaybackPolicy) {
        val p = policy.clamp()
        val k = SettingsKeys
        prefs[k.TTS_STREAM] = p.stream.name
        prefs[k.TTS_DELAY] = p.delaySeconds
        prefs[k.TTS_MAX] = p.maxLength
        prefs[k.TTS_FOCUS] = p.audioFocus
        prefs[k.TTS_EMOJI] = p.speakEmojis
        prefs[k.TTS_REPEAT] = p.repeatMinutes
        prefs[k.TTS_REPEAT_COUNT] = p.repeatCount
        prefs[k.TTS_PAUSE] = p.pauseMedia
        prefs[k.TTS_PITCH] = (p.voice.pitch * 100).toInt()
        prefs[k.TTS_LANG] = p.voice.languageTag
        prefs[ENGINE] = p.voice.engine
        prefs[VOICE_NAME] = p.voice.voiceName
        prefs[MIN_QUALITY] = p.voice.minQuality
    }
}
