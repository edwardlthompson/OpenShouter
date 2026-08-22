package org.openshouter.backup

import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object ShouterLegacyRoot {
    val PACKAGES = listOf("com.bhkapps.proshouter", "com.bhkapps.shouter")

    fun available(exec: (String) -> ByteArray? = ::suBytes): Boolean =
        exec("id")?.decodeToString()?.contains("uid=0") == true

    fun readDump(exec: (String) -> ByteArray? = ::suBytes): LegacyDump {
        var acc = LegacyDump()
        for (pkg in PACKAGES) {
            exec("cat /data/data/$pkg/databases/shdb")?.let { bytes ->
                ShouterLegacy.dumpFromSqlite(bytes)?.let { acc = acc.plus(it) }
            }
            exec("cat /data/data/$pkg/shared_prefs/${pkg}_preferences.xml")?.let { bytes ->
                ShouterLegacy.prefsFromXml(bytes)?.let { acc = acc.plus(LegacyDump(prefs = it)) }
            }
        }
        return acc
    }

    fun suBytes(command: String): ByteArray? {
        for (bin in SU_BINS) {
            for (args in listOf(listOf(bin, "-mm", "-c", command), listOf(bin, "-c", command))) {
                runSu(args)?.let { return it }
            }
        }
        return null
    }

    private val SU_BINS = listOf("/debug_ramdisk/su", "su", "/system/bin/su", "/system/xbin/su", "/sbin/su")

    private fun runSu(args: List<String>): ByteArray? = runCatching {
        val pb = ProcessBuilder(args).redirectErrorStream(true)
        runCatching { pb.environment()["PATH"] = "/debug_ramdisk:/system/bin:/system/xbin" }
        val proc = pb.start()
        val out = ByteArrayOutputStream()
        proc.inputStream.copyTo(out)
        if (!proc.waitFor(8, TimeUnit.SECONDS)) {
            proc.destroyForcibly()
            return null
        }
        if (proc.exitValue() != 0) return null
        out.toByteArray().takeIf { it.isNotEmpty() }
    }.getOrNull()
}
