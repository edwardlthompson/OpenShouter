package org.openshouter.service

import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.audio.AudioRouteMonitor
import org.openshouter.domain.AnnouncementGate
import org.openshouter.domain.AppSettings
import org.openshouter.geo.GeoMonitor

@Singleton
class SpeakGate @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audio: AudioRouteMonitor,
    private val geo: GeoMonitor,
) {
    suspend fun allow(settings: AppSettings): Boolean {
        val cal = Calendar.getInstance()
        val minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return AnnouncementGate.allow(
            settings,
            minute,
            day,
            pm.isInteractive,
            audio.headsetConnected(),
            geo.insideSilent(),
        )
    }
}
