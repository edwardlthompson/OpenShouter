package org.openshouter.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Crash-safe TTS create/shutdown with one visible retry. */
internal class TtsEngineBinder(
    private val appContext: Context,
    private val progressListener: UtteranceProgressListener,
    private val onReady: () -> Unit,
) {
    @Volatile var engine: TextToSpeech? = null
        private set
    @Volatile var boundEngine: String? = null
        private set
    @Volatile var ready = false
        private set
    @Volatile private var bindRetried = false
    private val _bindPhase = MutableStateFlow(TtsBindPhase.Ready)
    val bindPhase: StateFlow<TtsBindPhase> = _bindPhase
    private val _engineGen = MutableStateFlow(0)
    val engineGen: StateFlow<Int> = _engineGen

    fun warmup(enginePackage: String = boundEngine.orEmpty()) {
        bindEngine(enginePackage.trim(), resetRetry = true)
    }

    fun shutdown() {
        runCatching { engine?.shutdown() }
        engine = null
        boundEngine = null
        ready = false
        bindRetried = false
        _bindPhase.value = TtsBindPhase.Ready
    }

    private fun bindEngine(want: String, resetRetry: Boolean) {
        if (engine != null && boundEngine == want && ready) return
        if (resetRetry) bindRetried = false
        val old = engine
        ready = false
        engine = null
        boundEngine = want
        try {
            engine = TtsEngine.create(appContext, want) { status ->
                onBindResult(want, status == TextToSpeech.SUCCESS)
            }
        } catch (_: Throwable) {
            onBindThrowable(want)
        } finally {
            runCatching { old?.shutdown() }
        }
    }

    private fun onBindResult(want: String, success: Boolean) {
        when (TtsBindPolicy.afterInit(success, bindRetried)) {
            TtsBindPhase.Ready -> finishBindOk()
            TtsBindPhase.RetryOnce -> {
                bindRetried = true
                _bindPhase.value = TtsBindPhase.RetryOnce
                runCatching { engine?.shutdown() }
                engine = null
                ready = false
                bindEngine(if (want.isNotEmpty()) "" else want, resetRetry = false)
            }
            TtsBindPhase.FailedVisible -> {
                ready = false
                _bindPhase.value = TtsBindPhase.FailedVisible
                _engineGen.value += 1
            }
        }
    }

    private fun onBindThrowable(want: String) {
        when (TtsBindPolicy.afterThrowable(bindRetried)) {
            TtsBindPhase.RetryOnce -> {
                bindRetried = true
                _bindPhase.value = TtsBindPhase.RetryOnce
                bindEngine(if (want.isNotEmpty()) "" else want, resetRetry = false)
            }
            else -> {
                ready = false
                _bindPhase.value = TtsBindPhase.FailedVisible
                _engineGen.value += 1
            }
        }
    }

    private fun finishBindOk() {
        ready = true
        bindRetried = false
        _bindPhase.value = TtsBindPhase.Ready
        runCatching {
            engine?.language = Locale.getDefault()
            engine?.setOnUtteranceProgressListener(progressListener)
        }
        _engineGen.value += 1
        onReady()
    }
}
