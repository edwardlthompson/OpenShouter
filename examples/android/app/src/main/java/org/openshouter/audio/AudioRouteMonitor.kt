package org.openshouter.audio

import android.app.NotificationManager
import android.content.Context
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
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notify = context.getSystemService(NotificationManager::class.java)

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
