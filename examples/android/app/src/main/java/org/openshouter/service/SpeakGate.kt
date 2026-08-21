package org.openshouter.service

import android.content.Context
import android.os.PowerManager
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.audio.AudioRouteMonitor
import org.openshouter.domain.AnnouncementGate
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.IgnoreReason
import org.openshouter.domain.ShoutChannel
import org.openshouter.geo.GeoMonitor

@Singleton
class SpeakGate @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audio: AudioRouteMonitor,
    private val geo: GeoMonitor,
) {
    suspend fun allow(settings: AppSettings, channel: ShoutChannel? = null): Boolean =
        denyReason(settings, channel) == null

    suspend fun denyReason(
        settings: AppSettings,
        channel: ShoutChannel? = null,
        silentExempt: Boolean = false,
    ): IgnoreReason? {
        val cal = Calendar.getInstance()
        val minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val silent = audio.isSilent() && !silentExempt
        val inCall = runCatching {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            @Suppress("DEPRECATION")
            tm.callState != TelephonyManager.CALL_STATE_IDLE
        }.getOrDefault(false)
        val resolved = if (channel == null) {
            settings.deviceState
        } else {
            ChannelStates.resolve(
                settings.channelStates,
                channel,
                settings.deviceState,
                settings.ttsPlayback,
            ).device
        }
        val device = resolved.copy(
            allowSilentVibrate = resolved.allowSilentVibrate && settings.deviceState.allowSilentVibrate,
        )
        return AnnouncementGate.denyReason(
            settings,
            minute,
            day,
            pm.isInteractive,
            audio.headsetConnected(),
            geo.insideSilent(),
            silent,
            inCall,
            device,
        )
    }
}
