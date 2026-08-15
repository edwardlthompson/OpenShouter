package org.openshouter.tts

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import java.util.Locale
import org.openshouter.domain.TtsStream
import org.openshouter.domain.TtsVoice

internal object TtsEngine {
    fun applyStream(tts: TextToSpeech, stream: TtsStream) {
        val usage = when (stream) {
            TtsStream.MEDIA -> AudioAttributes.USAGE_MEDIA
            TtsStream.ALARM -> AudioAttributes.USAGE_ALARM
            TtsStream.NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
        }
        tts.setAudioAttributes(
            AudioAttributes.Builder().setUsage(usage).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(),
        )
    }

    fun languageTags(tts: TextToSpeech?): List<String> {
        val locales = tts?.availableLanguages ?: return emptyList()
        return locales.map { it.toLanguageTag() }.filter { it.isNotBlank() }.distinct().sorted().take(40)
    }

    fun applyVoice(tts: TextToSpeech, voice: TtsVoice) {
        tts.setPitch(voice.pitch)
        val tag = voice.languageTag
        if (tag.isNotBlank()) {
            runCatching { tts.language = Locale.forLanguageTag(tag) }
        }
    }

    fun requestFocus(audio: AudioManager, pauseMedia: Boolean): AudioFocusRequest? {
        if (Build.VERSION.SDK_INT < 26) return null
        val gain = if (pauseMedia) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        } else {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        }
        val req = AudioFocusRequest.Builder(gain)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .build()
        runCatching { audio.requestAudioFocus(req) }
        return req
    }

    fun abandonFocus(audio: AudioManager, request: AudioFocusRequest?) {
        val req = request ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            runCatching { audio.abandonAudioFocusRequest(req) }
        }
    }
}
