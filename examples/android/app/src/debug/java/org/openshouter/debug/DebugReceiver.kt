package org.openshouter.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import dagger.hilt.android.EntryPointAccessors
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.service.OpenShouterRuntime

class DebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ep = EntryPointAccessors.fromApplication(
            context.applicationContext,
            OpenShouterEntryPoint::class.java,
        )
        OpenShouterRuntime.ensureStarted(context)
        when (intent.action) {
            "org.openshouter.debug.RING" -> ep.calls().onState(
                TelephonyManager.CALL_STATE_RINGING,
                intent.getStringExtra("number").orEmpty(),
            )
            "org.openshouter.debug.IDLE" -> ep.calls().onState(TelephonyManager.CALL_STATE_IDLE, "")
            "org.openshouter.debug.INTERRUPT" -> ep.tts().interrupt()
        }
    }
}
