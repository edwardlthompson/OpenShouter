package org.openshouter.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.domain.SpokenEvent

@Singleton
class TtsController @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val appContext = context.applicationContext
    @Volatile private var engine: TextToSpeech? = null
    @Volatile private var ready = false
    @Volatile private var looping: SpokenEvent? = null
    @Volatile private var pending: SpokenEvent? = null

    fun warmup() {
        if (engine != null) return
        engine = TextToSpeech(appContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            engine?.language = Locale.getDefault()
            engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onError(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    val again = looping ?: return
                    speak(again)
                }
            })
            pending?.let { queued ->
                pending = null
                speak(queued)
            }
        }
    }

    fun speak(event: SpokenEvent) {
        warmup()
        val tts = engine
        if (tts == null || !ready) {
            pending = event
            return
        }
        pending = null
        looping = event.takeIf { it.looping }
        tts.speak(event.utterance, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    fun interrupt() {
        looping = null
        engine?.stop()
    }

    fun shutdown() {
        looping = null
        engine?.shutdown()
        engine = null
        ready = false
    }
}
