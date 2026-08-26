package org.openshouter.tts

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import org.openshouter.domain.TtsStream

/** Active MediaSession so the head unit switches to Media and AudioHardening allows playback. */
internal class TtsSpeakSession(context: Context) {
    private val title = context.packageManager.getApplicationLabel(context.applicationInfo).toString()
    private val session = MediaSession(context, "openshouter-tts").apply {
        setCallback(object : MediaSession.Callback() {})
        setPlaybackToLocal(TtsEngine.attributes(TtsStream.MEDIA))
        setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, title)
                .build(),
        )
    }

    fun start() {
        session.setPlaybackState(
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 1f)
                .setActions(PLAY_ACTIONS)
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

    private companion object {
        const val PLAY_ACTIONS = PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_STOP or
            PlaybackState.ACTION_PLAY_PAUSE
    }
}
