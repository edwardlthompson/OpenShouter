package org.openshouter.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object OpenShouterRuntime {
    fun ensureStarted(context: Context) {
        val intent = Intent(context, AnnouncerService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
}
