package org.openshouter.power

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openshouter.data.HistoryDao
import org.openshouter.data.SettingsRepository
import org.openshouter.data.ShoutHistoryStore
import org.openshouter.domain.PowerEvent
import org.openshouter.domain.PowerKind
import org.openshouter.domain.PowerRules
import org.openshouter.domain.SpokenEvent
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

@Singleton
class PowerMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val tts: TtsController,
    private val gate: SpeakGate,
    private val history: HistoryDao,
) : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastFull = false
    private var lastLow = false
    private var lastPercent: Int? = null
    @Volatile private var started = false

    fun start() {
        if (started) return
        started = true
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.registerReceiver(this, filter)
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        scope.launch {
            val snap = settings.snapshot()
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> if (snap.muteOnScreenOn) tts.interrupt()
                Intent.ACTION_SCREEN_OFF -> if (snap.muteOnScreenOff) tts.interrupt()
                Intent.ACTION_POWER_CONNECTED -> speak(snap, PowerEvent(PowerKind.CONNECTED, null))
                Intent.ACTION_POWER_DISCONNECTED -> speak(snap, PowerEvent(PowerKind.DISCONNECTED, null))
                Intent.ACTION_BATTERY_LOW -> speak(snap, PowerEvent(PowerKind.LOW, percent(intent)))
                Intent.ACTION_BATTERY_CHANGED -> onChanged(snap, intent)
            }
        }
    }

    private suspend fun onChanged(snap: org.openshouter.domain.AppSettings, intent: Intent) {
        val pct = percent(intent) ?: return
        if (snap.lowBatteryAlert && PowerRules.isLowThreshold(pct, snap) && !lastLow) {
            lastLow = true
            speak(snap, PowerEvent(PowerKind.LOW, pct))
        }
        if (pct > snap.batteryLowPercent) lastLow = false
        if (PowerRules.isFullThreshold(pct, snap) && !lastFull) {
            lastFull = true
            speak(snap, PowerEvent(PowerKind.FULL, pct))
        }
        if (pct < snap.batteryFullPercent) lastFull = false
        val prev = lastPercent
        lastPercent = pct
        val charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        if (charging && prev != null && prev != pct) {
            speak(snap, PowerEvent(PowerKind.LEVEL, pct))
        }
    }

    private suspend fun speak(snap: org.openshouter.domain.AppSettings, event: PowerEvent) {
        if (event.kind == PowerKind.CONNECTED || event.kind == PowerKind.DISCONNECTED) {
            if (!snap.powerConnectAlert) return
        }
        if (!gate.allow(snap, org.openshouter.domain.ShoutChannel.BATTERY)) return
        val phrase = snap.batteryPhrases.spoken(event)
        if (phrase.isBlank()) return
        ShoutHistoryStore.insertOnce(history, SpokenEvent.Kind.POWER, phrase)
        tts.speak(
            org.openshouter.domain.ChannelStates.spoken(
                snap,
                org.openshouter.domain.ShoutChannel.BATTERY,
                SpokenEvent.Kind.POWER,
                phrase,
            ),
        )
    }

    private fun percent(intent: Intent): Int? {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return null
        return (level * 100) / scale
    }
}
