package org.openshouter.backup

import android.content.ContentResolver
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream
import org.openshouter.domain.AppSpeakRule

object ShouterLegacy {
    val AUTHORITIES = listOf("bhkapps.proshouter", "bhkapps.shouter")

    fun isSqlite(bytes: ByteArray): Boolean = ShouterLegacyParse.isSqlite(bytes)

    fun dumpFromBytes(bytes: ByteArray): LegacyDump? {
        if (isSqlite(bytes)) return dumpFromSqlite(bytes)
        ShouterLegacyParse.prefsFromXml(bytes)?.let { return LegacyDump(prefs = it) }
        return dumpFromZip(bytes)
    }

    fun dumpFromSqlite(bytes: ByteArray): LegacyDump? {
        if (!isSqlite(bytes)) return null
        val tmp = File.createTempFile("os-shdb", ".db")
        return try {
            tmp.writeBytes(bytes)
            ShouterLegacyDb.fromPath(tmp.absolutePath)
        } catch (_: Exception) {
            LegacyDump()
        } finally {
            tmp.delete()
        }
    }

    fun rulesFromSqlite(bytes: ByteArray): List<AppSpeakRule>? = dumpFromSqlite(bytes)?.rules

    fun queryDump(resolver: ContentResolver): LegacyDump {
        for (auth in AUTHORITIES) {
            ShouterLegacyDb.fromProvider(resolver, auth)?.let { return it }
        }
        return LegacyDump()
    }

    fun queryBest(resolver: ContentResolver): LegacyDump {
        val rooted = ShouterLegacyRoot.readDump()
        if (rooted.rules.isNotEmpty() && rooted.prefs.isNotEmpty()) return rooted
        val oem = queryDump(resolver)
        if (rooted.prefs.isEmpty() && rooted.rules.isEmpty()) return oem
        return if (rooted.rules.isEmpty()) oem.plus(LegacyDump(prefs = rooted.prefs)) else rooted
    }

    fun queryInstalled(resolver: ContentResolver): List<AppSpeakRule> = queryDump(resolver).rules

    fun rulesFromRows(rows: List<Pair<String, String>>) = ShouterLegacyParse.rulesFromRows(rows)

    fun shoutEnabled(raw: String) = ShouterLegacyParse.shoutEnabled(raw)

    fun validPackage(name: String) = ShouterLegacyParse.validPackage(name)

    fun prefsFromXml(bytes: ByteArray) = ShouterLegacyParse.prefsFromXml(bytes)

    private fun dumpFromZip(bytes: ByteArray): LegacyDump? {
        if (bytes.size < 4 || bytes[0] != 0x50.toByte()) return null
        var acc = LegacyDump()
        var any = false
        runCatching {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    dumpFromBytes(zip.readBytes())?.let {
                        acc = acc.plus(it)
                        any = true
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        return acc.takeIf { any }
    }
}
