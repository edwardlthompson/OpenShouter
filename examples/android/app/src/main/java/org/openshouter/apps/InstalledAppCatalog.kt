package org.openshouter.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

data class InstalledApp(
    val packageName: String,
    val label: String,
)

object InstalledAppCatalog {
    fun list(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val infos = installed(pm)
        return infos.map { info ->
            val label = runCatching { pm.getApplicationLabel(info).toString() }
                .getOrDefault(info.packageName)
                .ifBlank { info.packageName }
            InstalledApp(info.packageName, label)
        }.distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun installed(pm: PackageManager): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }
    }
}
