package org.openshouter.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FossWearReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_WEAR_MUTE = "org.openshouter.wear.MUTE"
        const val ACTION_WEAR_UNMUTE = "org.openshouter.wear.UNMUTE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // Pure FOSS local broadcast hook for companion wear devices without proprietary GMS
    }
}
