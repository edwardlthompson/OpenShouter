package org.openshouter.tts

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import org.openshouter.domain.TtsEngineChoice
import org.openshouter.domain.TtsVoiceCandidate

internal object TtsEngines {
    fun installed(context: Context): List<TtsEngineChoice> {
        val pm = context.packageManager
        val found = pm.queryIntentServices(Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0)
        return found.map { info ->
            val pkg = info.serviceInfo.packageName
            val label = info.loadLabel(pm).toString().ifBlank { pkg }
            TtsEngineChoice(pkg, label)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    fun voices(tts: TextToSpeech?): List<TtsVoiceCandidate> =
        tts?.voices.orEmpty().map {
            TtsVoiceCandidate(
                it.name,
                it.locale.toLanguageTag(),
                it.quality,
                it.latency,
                it.isNetworkConnectionRequired,
            )
        }.filter { it.name.isNotBlank() }.distinctBy { it.name }.take(80)
}
