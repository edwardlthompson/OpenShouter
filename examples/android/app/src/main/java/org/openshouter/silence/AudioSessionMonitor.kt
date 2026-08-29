package org.openshouter.silence

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openshouter.data.SoundLeakDao
import org.openshouter.data.SoundLeakEntity

@Singleton
class AudioSessionMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val leaks: SoundLeakDao,
) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    private val callback = object : AudioManager.AudioPlaybackCallback() {
        override fun onPlaybackConfigChanged(configs: List<android.media.AudioPlaybackConfiguration>) {
            if (Build.VERSION.SDK_INT < 31) return
            val ourUid = Process.myUid()
            configs.forEach { config ->
                val uid = clientUid(config)
                val usage = config.audioAttributes.usage
                if (!AudioSessionHint.shouldRecord(uid, ourUid, usage)) return@forEach
                val pkg = context.packageManager.getPackagesForUid(uid)?.firstOrNull() ?: return@forEach
                if (AudioSessionHint.skipPackage(pkg, context.packageName)) return@forEach
                scope.launch {
                    leaks.upsert(
                        SoundLeakEntity(
                            pkg,
                            AudioSessionHint.CHANNEL_OWN,
                            "",
                            SoundEvidence.OWN_AUDIO.name,
                            System.currentTimeMillis(),
                        ),
                    )
                    leaks.pruneTo(50)
                }
            }
        }
    }

    fun start() {
        if (started || audio == null) return
        started = true
        runCatching { audio.registerAudioPlaybackCallback(callback, handler) }
    }

    private fun clientUid(config: android.media.AudioPlaybackConfiguration): Int =
        runCatching {
            config.javaClass.getMethod("getClientUid").invoke(config) as Int
        }.getOrDefault(-1)
}
