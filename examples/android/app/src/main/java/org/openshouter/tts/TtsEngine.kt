package org.openshouter.tts

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.io.File
import java.util.HashMap
import java.util.Locale
import org.openshouter.domain.TtsStream
import org.openshouter.domain.TtsVoice
import org.openshouter.domain.playable

internal object TtsEngine {
    fun resolveStream(
        audio: AudioManager,
        preferred: TtsStream,
        allowSilentVibrate: Boolean,
    ): TtsStream = preferred.playable(
        isMuted(audio, preferred),
        isMuted(audio, TtsStream.MEDIA),
        audio.ringerMode != AudioManager.RINGER_MODE_NORMAL,
        allowSilentVibrate,
        isMuted(audio, TtsStream.ALARM),
    )

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

    fun synthesizeToFile(tts: TextToSpeech, text: String, file: File, utteranceId: String) {
        if (Build.VERSION.SDK_INT >= 30) {
            tts.synthesizeToFile(text, Bundle(), file, utteranceId)
        } else {
            @Suppress("DEPRECATION")
            tts.synthesizeToFile(
                text,
                HashMap<String, String>().apply {
                    put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                },
                file.absolutePath,
            )
        }
    }

    fun applyVoice(tts: TextToSpeech, voice: TtsVoice) {
        tts.setPitch(voice.pitch)
        val tag = voice.languageTag
        if (tag.isNotBlank()) {
            runCatching { tts.language = Locale.forLanguageTag(tag) }
        }
    }

    fun requestFocus(audio: AudioManager, pauseMedia: Boolean, stream: TtsStream): AudioFocusRequest? {
        if (Build.VERSION.SDK_INT < 26) return null
        val gain = if (pauseMedia) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        } else {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        }
        val usage = when (stream) {
            TtsStream.MEDIA -> AudioAttributes.USAGE_MEDIA
            TtsStream.ALARM -> AudioAttributes.USAGE_ALARM
            TtsStream.NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
        }
        val req = AudioFocusRequest.Builder(gain)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .build()
        runCatching { audio.requestAudioFocus(req) }
        return req
    }

    private fun isMuted(audio: AudioManager, stream: TtsStream): Boolean {
        val type = when (stream) {
            TtsStream.MEDIA -> AudioManager.STREAM_MUSIC
            TtsStream.ALARM -> AudioManager.STREAM_ALARM
            TtsStream.NOTIFICATION -> AudioManager.STREAM_NOTIFICATION
        }
        return audio.isStreamMute(type) || audio.getStreamVolume(type) <= 0
    }

    fun abandonFocus(audio: AudioManager, request: AudioFocusRequest?) {
        val req = request ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            runCatching { audio.abandonAudioFocusRequest(req) }
        }
    }
}
