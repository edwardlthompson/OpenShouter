package org.openshouter.tts

import android.content.Context
import android.media.MediaPlayer
import android.os.PowerManager
import java.io.File
import org.openshouter.domain.TtsStream

internal class TtsFilePlayer(private val context: Context) {
    private val session = TtsSpeakSession(context)
    private var player: MediaPlayer? = null

    fun play(file: File, stream: TtsStream, times: Int, onComplete: () -> Unit) {
        stop()
        if (!file.exists() || file.length() <= 0L) {
            onComplete()
            return
        }
        session.start()
        playOnce(file, stream, times.coerceAtLeast(1), onComplete)
    }

    fun stop() {
        runCatching {
            player?.stop()
            player?.release()
        }
        player = null
        session.stop()
    }

    fun release() {
        stop()
        session.release()
    }

    private fun playOnce(file: File, stream: TtsStream, left: Int, onComplete: () -> Unit) {
        val next = MediaPlayer()
        player = next
        next.setAudioAttributes(TtsEngine.attributes(stream))
        next.setVolume(1f, 1f)
        next.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        next.setOnCompletionListener {
            if (left > 1) playOnce(file, stream, left - 1, onComplete) else {
                stop()
                onComplete()
            }
        }
        next.setOnErrorListener { _, _, _ ->
            stop()
            onComplete()
            true
        }
        runCatching {
            next.setDataSource(file.absolutePath)
            next.prepare()
            next.start()
        }.onFailure {
            stop()
            onComplete()
        }
    }
}
