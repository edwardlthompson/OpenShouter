package org.openshouter.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import android.telephony.TelephonyManager
import dagger.hilt.android.AndroidEntryPoint
import dev.foss.goldenpath.BuildConfig
import dev.foss.goldenpath.MainActivity
import dev.foss.goldenpath.R
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openshouter.audio.AudioRouteMonitor
import org.openshouter.bluetooth.BluetoothMonitor
import org.openshouter.calendar.CalendarMonitor
import org.openshouter.call.CallMonitor
import org.openshouter.data.SettingsRepository
import org.openshouter.geo.GeoMonitor
import org.openshouter.gesture.GestureMonitor
import org.openshouter.power.PowerMonitor
import org.openshouter.time.TimeShoutMonitor
import org.openshouter.time.TimeShoutScheduler
import org.openshouter.tts.TtsController
import org.openshouter.domain.SpokenEvent

@AndroidEntryPoint
class AnnouncerService : Service() {
    @Inject lateinit var tts: TtsController
    @Inject lateinit var calls: CallMonitor
    @Inject lateinit var power: PowerMonitor
    @Inject lateinit var gestures: GestureMonitor
    @Inject lateinit var geo: GeoMonitor
    @Inject lateinit var audio: AudioRouteMonitor
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var timeShout: TimeShoutScheduler
    @Inject lateinit var timeTicks: TimeShoutMonitor
    @Inject lateinit var calendar: CalendarMonitor
    @Inject lateinit var bluetooth: BluetoothMonitor
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.service_running))
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(
                this,
                41,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(41, notification)
        }
        tts.warmup()
        runCatching { calls.start() }
        runCatching { power.start() }
        runCatching { gestures.start() }
        runCatching { geo.start() }
        runCatching { audio.start { } }
        runCatching { calendar.start() }
        runCatching { bluetooth.start() }
        runCatching { timeTicks.start() }
        scope.launch { timeShout.sync(settings.snapshot()) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching { calls.start() }
        when (intent?.action) {
            ACTION_TTS_TEST -> tts.speak(
                SpokenEvent(SpokenEvent.Kind.NOTIFICATION, getString(R.string.tts_test_phrase)),
                immediate = true,
            )
            else -> if (BuildConfig.DEBUG) when (intent?.action) {
                ACTION_DEBUG_RING -> calls.onState(
                    TelephonyManager.CALL_STATE_RINGING,
                    intent.getStringExtra(EXTRA_NUMBER).orEmpty(),
                )
                ACTION_DEBUG_IDLE -> calls.onState(TelephonyManager.CALL_STATE_IDLE, "")
                ACTION_DEBUG_INTERRUPT -> tts.interrupt()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        gestures.stop()
        tts.shutdown()
        super.onDestroy()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.service_channel), NotificationManager.IMPORTANCE_LOW),
        )
    }

    companion object {
        const val CHANNEL = "openshouter-fg"
        const val ACTION_TTS_TEST = "org.openshouter.action.TTS_TEST"
        const val ACTION_DEBUG_RING = "org.openshouter.debug.RING"
        const val ACTION_DEBUG_IDLE = "org.openshouter.debug.IDLE"
        const val ACTION_DEBUG_INTERRUPT = "org.openshouter.debug.INTERRUPT"
        const val EXTRA_NUMBER = "number"
    }
}
