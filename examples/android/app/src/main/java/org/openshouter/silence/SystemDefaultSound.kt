package org.openshouter.silence

import android.content.Context
import android.media.RingtoneManager

object SystemDefaultSound {
    fun notificationIsSilent(context: Context): Boolean {
        val uri = runCatching {
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)
        }.getOrNull()
        return isSilent(uri?.toString(), OemSilenceHints.currentNeedsSilentFile())
    }

    fun isSilent(uri: String?, oemUnreliableNone: Boolean): Boolean {
        val text = uri?.trim().orEmpty()
        if (SilentWav.isSilentUri(text) && text.isNotEmpty() && !text.equals("null", true)) return true
        if (text.isEmpty() || text.equals("null", true)) return !oemUnreliableNone
        return false
    }
}
