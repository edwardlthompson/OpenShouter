package org.openshouter.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import org.openshouter.domain.CarAudioRoute

/** Reads Android Auto / DHU projection via the host CarConnection provider. */
internal object CarProjection {
    private val URI = Uri.parse("content://androidx.car.app.connection")
    const val COLUMN = "CarConnectionState"
    const val ACTION = "androidx.car.app.connection.action.CAR_CONNECTION_UPDATED"

    fun connected(context: Context): Boolean =
        CarAudioRoute.projectionConnected(state(context))

    fun state(context: Context): Int = runCatching {
        context.contentResolver.query(URI, arrayOf(COLUMN), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use 0
            val idx = cursor.getColumnIndex(COLUMN)
            if (idx >= 0) cursor.getInt(idx) else cursor.getInt(0)
        } ?: 0
    }.getOrDefault(0)

    fun observe(context: Context, onChange: () -> Unit) {
        val app = context.applicationContext
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = onChange()
        }
        runCatching { app.contentResolver.registerContentObserver(URI, true, observer) }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) = onChange()
        }
        ContextCompat.registerReceiver(app, receiver, IntentFilter(ACTION), ContextCompat.RECEIVER_EXPORTED)
    }
}
