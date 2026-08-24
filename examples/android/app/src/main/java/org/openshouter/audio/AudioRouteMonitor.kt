package org.openshouter.audio

import android.app.NotificationManager
import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.domain.RingerSilent

@Singleton
class AudioRouteMonitor @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val app = context.applicationContext
    private val audio = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notify = app.getSystemService(NotificationManager::class.java)
    private val uiMode = app.getSystemService(UiModeManager::class.java)

    fun interruptionFilter(): Int =
        notify?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL

    fun isPriorityDnd(): Boolean =
        interruptionFilter() == NotificationManager.INTERRUPTION_FILTER_PRIORITY

    fun isSilent(): Boolean {
        val filter = interruptionFilter()
        val dnd = filter != NotificationManager.INTERRUPTION_FILTER_ALL &&
            filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        return RingerSilent.active(audio.ringerMode == AudioManager.RINGER_MODE_NORMAL, dnd)
    }

    fun headsetConnected(): Boolean {
        val devices = audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any { it.isHeadsetLike() }
    }

    fun carModeActive(): Boolean {
        if (uiMode?.currentModeType == Configuration.UI_MODE_TYPE_CAR) return true
        if (app.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) return true
        return audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { it.isCarRoute() }
    }

    fun start(onChange: () -> Unit) {
        if (Build.VERSION.SDK_INT >= 23) {
            audio.registerAudioDeviceCallback(object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>?) = onChange()
                override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>?) = onChange()
            }, null)
        }
    }
}

private fun AudioDeviceInfo.isHeadsetLike(): Boolean {
    val type = type
    if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
        type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
        type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
        type == AudioDeviceInfo.TYPE_USB_HEADSET
    ) {
        return true
    }
    return Build.VERSION.SDK_INT >= 31 && type == AudioDeviceInfo.TYPE_BLE_HEADSET
}

private fun AudioDeviceInfo.isCarRoute(): Boolean = type == AudioDeviceInfo.TYPE_BUS
