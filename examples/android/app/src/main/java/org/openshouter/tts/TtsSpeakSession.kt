package org.openshouter.tts

import android.content.Context
import android.media.session.MediaSession
import android.media.session.PlaybackState

/** Active MediaSession so Android 16 AudioHardening treats in-process TTS as media, not background. */
internal class TtsSpeakSession(context: Context) {
    private val session = MediaSession(context, "openshouter-tts").apply {
        setCallback(object : MediaSession.Callback() {})
    }

    fun start() {
        session.setPlaybackState(
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 1f)
                .setActions(PlaybackState.ACTION_STOP)
                .build(),
        )
        session.isActive = true
    }

    fun stop() {
        session.isActive = false
        session.setPlaybackState(
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_STOPPED, 0L, 1f)
                .build(),
        )
    }

    fun release() {
        stop()
        session.release()
    }
}
