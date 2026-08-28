package org.openshouter.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.foss.goldenpath.R
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openshouter.data.HistoryDao
import org.openshouter.data.SettingsRepository
import org.openshouter.data.ShoutHistoryStore
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

@Singleton
class BluetoothMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val tts: TtsController,
    private val gate: SpeakGate,
    private val history: HistoryDao,
) : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile private var started = false

    fun start() {
        if (started) return
        if (!hasConnect()) return
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(ACTION_BATTERY)
        }
        context.registerReceiver(this, filter)
        started = true
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        scope.launch { handle(intent) }
    }

    private suspend fun handle(intent: Intent) {
        val snap = settings.snapshot()
        if (!gate.allow(snap, ShoutChannel.BLUETOOTH)) return
        val name = runCatching { device(intent)?.name }.getOrNull().orEmpty()
        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                if (!snap.bluetoothConnectAlert) return
                val label = BluetoothShout.deviceName(name) ?: return
                speak(snap, context.getString(R.string.bluetooth_connected, label))
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                if (!snap.bluetoothConnectAlert) return
                val label = BluetoothShout.deviceName(name) ?: return
                speak(snap, context.getString(R.string.bluetooth_disconnected, label))
            }
            ACTION_BATTERY -> {
                if (!snap.bluetoothBatteryAlert) return
                val percent = intent.getIntExtra(EXTRA_BATTERY, -1)
                if (!BluetoothShout.batteryOk(name, percent)) return
                val label = BluetoothShout.deviceName(name) ?: return
                speak(snap, context.getString(R.string.bluetooth_battery_level, label, percent))
            }
        }
    }

    private suspend fun speak(snap: AppSettings, phrase: String) {
        if (phrase.isBlank()) return
        ShoutHistoryStore.insertOnce(history, SpokenEvent.Kind.BLUETOOTH, phrase)
        tts.speak(ChannelStates.spoken(snap, ShoutChannel.BLUETOOTH, SpokenEvent.Kind.BLUETOOTH, phrase))
    }

    private fun device(intent: Intent): BluetoothDevice? =
        if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

    private fun hasConnect(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 31) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ACTION_BATTERY = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
        const val EXTRA_BATTERY = "android.bluetooth.device.extra.BATTERY_LEVEL"
    }
}
