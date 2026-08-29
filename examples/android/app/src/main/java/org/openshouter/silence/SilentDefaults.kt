package org.openshouter.silence

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings

object SilentDefaults {
    fun canWrite(context: Context): Boolean =
        Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context)

    fun setNotification(context: Context, uri: Uri): Boolean = set(context, RingtoneManager.TYPE_NOTIFICATION, uri)

    fun setRingtone(context: Context, uri: Uri): Boolean = set(context, RingtoneManager.TYPE_RINGTONE, uri)

    private fun set(context: Context, type: Int, uri: Uri): Boolean {
        if (!canWrite(context)) return false
        return runCatching {
            RingtoneManager.setActualDefaultRingtoneUri(context, type, uri)
            true
        }.getOrDefault(false)
    }
}
