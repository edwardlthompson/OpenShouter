package org.openshouter.tts

import android.content.Context
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
import org.openshouter.domain.TtsVoiceCandidate
import org.openshouter.domain.TtsVoicePick
import org.openshouter.domain.playable
import org.openshouter.domain.forCarPlayback

internal object TtsEngine {
    fun resolveStream(
        audio: AudioManager,
        preferred: TtsStream,
        allowSilentVibrate: Boolean,
        carMode: Boolean = false,
    ): TtsStream = preferred.playable(
        isMuted(audio, preferred),
        isMuted(audio, TtsStream.MEDIA),
        audio.ringerMode != AudioManager.RINGER_MODE_NORMAL,
        allowSilentVibrate,
    ).forCarPlayback(carMode)

    fun applyStream(tts: TextToSpeech, stream: TtsStream) {
        tts.setAudioAttributes(attributes(stream))
    }

    fun attributes(stream: TtsStream): AudioAttributes {
        val builder = AudioAttributes.Builder()
            .setUsage(usage(stream))
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        if (Build.VERSION.SDK_INT >= 32) {
            builder.setSpatializationBehavior(AudioAttributes.SPATIALIZATION_BEHAVIOR_NEVER)
        }
        return builder.build()
    }

    fun create(context: Context, enginePackage: String, onInit: (Int) -> Unit): TextToSpeech {
        val listener = TextToSpeech.OnInitListener(onInit)
        return if (enginePackage.isBlank()) {
            TextToSpeech(context, listener)
        } else {
            TextToSpeech(context, listener, enginePackage)
        }
    }

    fun languageTags(tts: TextToSpeech?): List<String> {
        val locales = tts?.availableLanguages ?: return emptyList()
        return locales.map { it.toLanguageTag() }.filter { it.isNotBlank() }.distinct().sorted().take(80)
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
        tts.setSpeechRate(1.0f)
        val tag = voice.languageTag
        val picked = TtsVoicePick.best(
            tts.voices.orEmpty().map {
                TtsVoiceCandidate(
                    it.name,
                    it.locale.toLanguageTag(),
                    it.quality,
                    it.latency,
                    it.isNetworkConnectionRequired,
                )
            },
            tag,
            voice.minQuality,
            voice.voiceName,
        )
        val engineVoice = picked?.let { choice -> tts.voices.firstOrNull { it.name == choice.name } }
        if (engineVoice != null) {
            tts.voice = engineVoice
        } else if (tag.isNotBlank()) {
            runCatching { tts.language = Locale.forLanguageTag(tag) }
        }
    }

    private fun usage(stream: TtsStream): Int = when (stream) {
        TtsStream.MEDIA -> AudioAttributes.USAGE_MEDIA
        TtsStream.ALARM -> AudioAttributes.USAGE_ALARM
        TtsStream.NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
    }

    fun requestFocus(audio: AudioManager, pauseMedia: Boolean, stream: TtsStream): AudioFocusRequest? {
        if (Build.VERSION.SDK_INT < 26) return null
        val gain = if (pauseMedia) {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        } else {
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        }
        val req = AudioFocusRequest.Builder(gain)
            .setAudioAttributes(attributes(stream))
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
