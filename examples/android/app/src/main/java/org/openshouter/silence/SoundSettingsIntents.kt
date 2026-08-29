package org.openshouter.silence

import android.content.Intent
import android.media.RingtoneManager
import android.provider.Settings

object SoundSettingsIntents {
    fun soundSettings(): Intent =
        Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun ringtonePicker(type: Int): Intent =
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, type)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun writeSettings(packageName: String): Intent =
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            .setData(android.net.Uri.parse("package:$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
