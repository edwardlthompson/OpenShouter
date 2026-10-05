package org.openshouter.tts

import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openshouter.call.CallLoopGate

internal object TtsUtteranceCallbacks {
    fun listener(
        scope: CoroutineScope,
        audio: AudioManager,
        playback: TtsPlayback,
        engine: () -> TextToSpeech?,
        ready: () -> Boolean,
        interrupt: () -> Unit,
    ): UtteranceProgressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit
        override fun onError(utteranceId: String?) {
            scope.launch(Dispatchers.Main.immediate) { playback.onSynthFailed(utteranceId) }
        }
        override fun onDone(utteranceId: String?) {
            scope.launch(Dispatchers.Main.immediate) {
                if (CallLoopGate.cutVoip(audio.mode == AudioManager.MODE_IN_COMMUNICATION, interrupt)) {
                    return@launch
                }
                playback.playSynthesized(engine(), ready(), utteranceId)
            }
        }
    }
}
