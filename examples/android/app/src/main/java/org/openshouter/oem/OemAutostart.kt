package org.openshouter.oem

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

object OemAutostart {
    fun isAggressiveOem(): Boolean {
        val mfr = Build.MANUFACTURER.lowercase()
        return mfr.contains("xiaomi") ||
            mfr.contains("huawei") ||
            mfr.contains("oppo") ||
            mfr.contains("vivo") ||
            mfr.contains("oneplus") ||
            mfr.contains("samsung")
    }

    fun autostartIntent(context: Context): Intent? {
        val mfr = Build.MANUFACTURER.lowercase()
        val intent = Intent()
        when {
            mfr.contains("xiaomi") -> intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            mfr.contains("oppo") -> intent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            mfr.contains("vivo") -> intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity")
            mfr.contains("huawei") -> intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
            else -> return null
        }
        return intent.takeIf {
            runCatching { context.packageManager.resolveActivity(it, 0) != null }.getOrDefault(false)
        }
    }

    fun settingsIntent(context: Context): Intent? =
        autostartIntent(context) ?: Intent(android.provider.Settings.ACTION_SETTINGS)
}
