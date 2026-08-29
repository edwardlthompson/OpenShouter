package org.openshouter.silence

/** ColorOS / OxygenOS often has no real None, or Silent still plays a ding. */
object OemSilenceHints {
    private val AOSP_ROMS = listOf("lineage", "crdroid", "calyx", "graphene", "divest", "iode")
    private val ROM_PROPS = listOf(
        "ro.lineage.version",
        "ro.lineage.device",
        "ro.lineage.build.version",
        "ro.build.flavor",
        "ro.modversion",
        "ro.calyxos.version",
        "ro.grapheneos.version",
    )

    fun needsSilentFile(manufacturer: String, romKeys: String = ""): Boolean {
        if (isAospCustomRom(romKeys)) return false
        val name = manufacturer.trim().lowercase()
        if (name.isEmpty()) return false
        return name.contains("oneplus") ||
            name.contains("oppo") ||
            name.contains("realme") ||
            name.contains("oplus")
    }

    fun currentNeedsSilentFile(): Boolean = needsSilentFile(android.os.Build.MANUFACTURER, romKeys())

    fun isAospCustomRom(romKeys: String): Boolean {
        val hay = romKeys.lowercase()
        return AOSP_ROMS.any { hay.contains(it) }
    }

    fun romKeys(): String = ROM_PROPS.joinToString(" ") { sysProp(it) }

    private fun sysProp(key: String): String = runCatching {
        val clz = Class.forName("android.os.SystemProperties")
        clz.getMethod("get", String::class.java, String::class.java).invoke(null, key, "") as? String
    }.getOrNull().orEmpty()
}
