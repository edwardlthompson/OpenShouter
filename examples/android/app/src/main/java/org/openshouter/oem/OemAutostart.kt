package org.openshouter.oem

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object OemAutostart {
    fun vendor(
        manufacturer: String = Build.MANUFACTURER.orEmpty(),
        brand: String = Build.BRAND.orEmpty(),
    ): String? {
        val blob = "${manufacturer.lowercase()} ${brand.lowercase()}"
        return when {
            "xiaomi" in blob || "miui" in blob || "redmi" in blob || "poco" in blob -> "xiaomi"
            "samsung" in blob -> "samsung"
            "huawei" in blob || "honor" in blob -> "huawei"
            "oppo" in blob || "realme" in blob -> "oppo"
            "vivo" in blob || "iqoo" in blob -> "vivo"
            "oneplus" in blob -> "oneplus"
            else -> null
        }
    }

    fun settingsIntent(): Intent? = runCatching {
        candidates("").firstOrNull() ?: batterySettings()
    }.getOrNull()

    fun settingsIntent(packageName: String): Intent? = runCatching {
        candidates(packageName).firstOrNull() ?: details(packageName)
    }.getOrNull()

    fun settingsIntent(context: Context): Intent? = runCatching {
        val pkg = context.packageName
        candidates(pkg).firstOrNull { resolvable(context, it) } ?: details(pkg)
    }.getOrNull()

    private fun candidates(packageName: String): List<Intent> {
        val found = when (vendor()) {
            "xiaomi" -> listOf(
                component("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
                action("miui.intent.action.OP_AUTO_START"),
                component("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
            )
            "samsung" -> listOf(
                component("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
                component("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"),
                component("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity"),
            )
            "huawei" -> listOf(
                component("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                component("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
                action("huawei.intent.action.HSM_BOOTAPP_MANAGER"),
            )
            "oppo" -> listOf(
                component("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
                component("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                component("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
            )
            "vivo" -> listOf(
                component("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
                component("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
                component("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
            )
            "oneplus" -> listOf(
                component("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
                component("com.oplus.safecenter", "com.oplus.safecenter.startupapp.view.StartupAppListActivity"),
                component("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            )
            else -> emptyList()
        }
        return found + batterySettings() + if (packageName.isNotEmpty()) listOf(details(packageName)) else emptyList()
    }

    private fun batterySettings(): Intent =
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun details(packageName: String): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun component(pkg: String, cls: String): Intent =
        Intent().setComponent(ComponentName(pkg, cls)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun action(name: String): Intent =
        Intent(name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    @Suppress("DEPRECATION", "QueryPermissionsNeeded")
    private fun resolvable(context: Context, intent: Intent): Boolean =
        runCatching {
            context.packageManager.resolveActivity(intent, 0) != null
        }.getOrDefault(false)
}
