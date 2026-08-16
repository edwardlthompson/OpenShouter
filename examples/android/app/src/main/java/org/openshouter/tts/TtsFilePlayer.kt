package org.openshouter.tts

import android.media.AudioAttributes
import android.media.MediaPlayer
import java.io.File
import org.openshouter.domain.TtsStream

internal class TtsFilePlayer {
    private var player: MediaPlayer? = null

    fun play(file: File, stream: TtsStream, times: Int, onComplete: () -> Unit) {
        stop()
        if (!file.exists() || file.length() <= 0L) {
            onComplete()
            return
        }
        playOnce(file, stream, times.coerceAtLeast(1), onComplete)
    }

    fun stop() {
        runCatching {
            player?.stop()
            player?.release()
        }
        player = null
    }

    private fun playOnce(file: File, stream: TtsStream, left: Int, onComplete: () -> Unit) {
        val next = MediaPlayer()
        player = next
        next.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(usage(stream))
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
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

    private fun usage(stream: TtsStream): Int = when (stream) {
        TtsStream.MEDIA -> AudioAttributes.USAGE_MEDIA
        TtsStream.ALARM -> AudioAttributes.USAGE_ALARM
        TtsStream.NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
    }
}
