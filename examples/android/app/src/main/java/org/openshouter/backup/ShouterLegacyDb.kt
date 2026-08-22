package org.openshouter.backup

import android.content.ContentResolver
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

internal object ShouterLegacyDb {
    fun fromPath(path: String): LegacyDump {
        val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
        return try {
            LegacyDump(
                rules = sql(db, "SELECT package, shout FROM apps", emptyList()) {
                    ShouterLegacyParse.rulesFromRows(pairs(it, "package", "shout"))
                },
                quietCells = sql(db, "SELECT hour, day FROM silenthours", emptyList()) { quietPairs(it) },
                reminders = sql(db, "SELECT text, en, type, sttime FROM reminders", emptyList()) { reminderRows(it) },
                nicks = sql(db, "SELECT numids, nickname, call_denied, sms_denied FROM contacts", emptyMap()) { nickMap(it) },
                blocked = sql(db, "SELECT numids, nickname, call_denied, sms_denied FROM contacts", emptySet()) { blockSet(it) },
            )
        } finally {
            db.close()
        }
    }

    fun fromProvider(resolver: ContentResolver, auth: String): LegacyDump? {
        val apps = query(resolver, auth, "apps") ?: return null
        return LegacyDump(
            rules = apps.use { ShouterLegacyParse.rulesFromRows(pairs(it, "package", "shout")) },
            quietCells = query(resolver, auth, "silenthours")?.use { quietPairs(it) }.orEmpty(),
            reminders = query(resolver, auth, "reminders")?.use { reminderRows(it) }.orEmpty(),
            nicks = query(resolver, auth, "contacts")?.use { nickMap(it) }.orEmpty(),
            blocked = query(resolver, auth, "contacts")?.use { blockSet(it) }.orEmpty(),
        )
    }

    private fun query(resolver: ContentResolver, auth: String, table: String): Cursor? =
        runCatching { resolver.query(Uri.parse("content://$auth/$table"), null, null, null, null) }.getOrNull()

    private fun <T> sql(db: SQLiteDatabase, q: String, fallback: T, read: (Cursor) -> T): T =
        try {
            db.rawQuery(q, null).use(read)
        } catch (_: Exception) {
            fallback
        }

    private fun pairs(cur: Cursor, a: String, b: String): List<Pair<String, String>> {
        val ia = cur.getColumnIndex(a)
        val ib = cur.getColumnIndex(b)
        if (ia < 0 || ib < 0) return emptyList()
        return buildList {
            while (cur.moveToNext()) add(cur.getString(ia).orEmpty() to cur.getString(ib).orEmpty())
        }
    }

    private fun quietPairs(cur: Cursor): List<Pair<Int, Int>> {
        val h = cur.getColumnIndex("hour")
        val d = cur.getColumnIndex("day")
        if (h < 0 || d < 0) return emptyList()
        return buildList { while (cur.moveToNext()) add(cur.getInt(h) to cur.getInt(d)) }
    }

    private fun reminderRows(cur: Cursor): List<LegacyReminder> {
        val t = cur.getColumnIndex("text")
        if (t < 0) return emptyList()
        val e = cur.getColumnIndex("en")
        val ty = cur.getColumnIndex("type")
        val s = cur.getColumnIndex("sttime")
        return buildList {
            while (cur.moveToNext()) {
                val text = cur.getString(t).orEmpty().trim()
                if (text.isEmpty()) continue
                add(LegacyReminder(text.take(200), e < 0 || cur.getInt(e) != 0, if (ty >= 0) cur.getInt(ty) else 1, if (s >= 0) cur.getLong(s) else 0L))
            }
        }
    }

    private fun nickMap(cur: Cursor): Map<String, String> {
        val n = cur.getColumnIndex("numids")
        val nick = cur.getColumnIndex("nickname")
        if (n < 0 || nick < 0) return emptyMap()
        val out = linkedMapOf<String, String>()
        while (cur.moveToNext()) {
            val name = cur.getString(nick).orEmpty().trim()
            if (name.isNotEmpty()) ShouterLegacyParse.digits(cur.getString(n)).forEach { out[it] = name.take(40) }
        }
        return out
    }

    private fun blockSet(cur: Cursor): Set<String> {
        val n = cur.getColumnIndex("numids")
        if (n < 0) return emptySet()
        val c = cur.getColumnIndex("call_denied")
        val s = cur.getColumnIndex("sms_denied")
        val out = linkedSetOf<String>()
        while (cur.moveToNext()) {
            val denied = (c >= 0 && cur.getInt(c) != 0) || (s >= 0 && cur.getInt(s) != 0)
            if (denied) ShouterLegacyParse.digits(cur.getString(n)).forEach { out.add(it) }
        }
        return out
    }
}
