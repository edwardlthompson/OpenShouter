package org.openshouter.backup

import android.database.sqlite.SQLiteDatabase
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.openshouter.domain.ReminderInterval
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ShouterLegacyTest {
    @Test
    fun shoutOneIsEnabled() {
        assertTrue(ShouterLegacy.shoutEnabled("1"))
        assertTrue(ShouterLegacy.shoutEnabled("true"))
        assertFalse(ShouterLegacy.shoutEnabled("0"))
        assertFalse(ShouterLegacy.shoutEnabled(""))
    }

    @Test
    fun packageMustBeDotted() {
        assertTrue(ShouterLegacy.validPackage("com.juggernaut_tech.rivercast"))
        assertFalse(ShouterLegacy.validPackage("bad"))
        assertFalse(ShouterLegacy.validPackage(""))
    }

    @Test
    fun sqliteImportsEnabledOnlyAndSkipsLogs() {
        val file = File.createTempFile("shdb", ".db")
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        db.execSQL("CREATE TABLE apps(package TEXT PRIMARY KEY, shout TEXT)")
        db.execSQL("CREATE TABLE shoutlogs(shout TEXT)")
        db.execSQL("INSERT INTO shoutlogs(shout) VALUES ('payload-must-not-be-read')")
        db.execSQL("INSERT INTO apps(package, shout) VALUES ('com.enabled.app', '1')")
        db.execSQL("INSERT INTO apps(package, shout) VALUES ('com.disabled.app', '0')")
        db.execSQL("INSERT INTO apps(package, shout) VALUES ('not-a-package', '1')")
        db.close()
        val bytes = file.readBytes()
        val rules = ShouterLegacy.rulesFromSqlite(bytes)
        assertEquals(1, rules!!.size)
        assertEquals("com.enabled.app", rules[0].packageName)
        assertTrue(rules[0].speakNotification)
        assertFalse(String(bytes).let { text ->
            ShouterLegacy.rulesFromRows(listOf("com.enabled.app" to "1")).any { it.packageName.contains("payload") }
        })
        file.delete()
    }

    @Test
    fun nonSqliteReturnsNull() {
        assertNull(ShouterLegacy.rulesFromSqlite("PK zip".toByteArray()))
        assertFalse(ShouterLegacy.isSqlite(SettingsBackup.toZip(org.openshouter.domain.AppSettings(), emptyList())))
    }

    @Test
    fun prefsXmlMapsMuteAndFormats() {
        val xml = """
            <map>
              <boolean name="Enmstcntrl" value="true" />
              <boolean name="Ennotiscrnoffonly" value="true" />
              <boolean name="Entimeshout" value="true" />
              <string name="Entssel">15</string>
              <string name="Ecllerprefix">Incoming call from</string>
              <string name="pkdbserprx_call">{"c":"secret"}</string>
            </map>
        """.trimIndent().toByteArray()
        val prefs = ShouterLegacy.prefsFromXml(xml)!!
        assertTrue(prefs.containsKey("Ennotiscrnoffonly"))
        assertFalse(prefs.containsKey("pkdbserprx_call"))
        val mapped = ShouterLegacyMap.map(LegacyDump(prefs = prefs))
        assertTrue(mapped.screenOffOnly)
        assertTrue(mapped.timeOn)
        assertEquals(15, mapped.timeEvery)
        assertTrue(mapped.callFormat.contains("%name"))
        assertTrue(mapped.speakApp)
        assertTrue(mapped.remindersOn)
    }

    @Test
    fun silentHourCellsBecomeAWindow() {
        val window = ShouterLegacyMap.quietWindow(listOf(22 to 1, 23 to 1, 22 to 2))!!
        assertEquals(22 * 60, window.first)
        assertEquals(0, window.second)
        assertEquals(setOf(1, 2), window.third)
    }

    @Test
    fun sqliteDumpSkipsLogsAndReadsReminders() {
        val file = File.createTempFile("shdb", ".db")
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        db.execSQL("CREATE TABLE apps(package TEXT PRIMARY KEY, shout TEXT)")
        db.execSQL("CREATE TABLE shoutlogs(shout TEXT)")
        db.execSQL("CREATE TABLE reminders(_id INTEGER PRIMARY KEY, text TEXT, en INTEGER, type INTEGER, sttime INTEGER)")
        db.execSQL("INSERT INTO shoutlogs(shout) VALUES ('payload-must-not-be-read')")
        db.execSQL("INSERT INTO apps(package, shout) VALUES ('com.enabled.app', '1')")
        db.execSQL("INSERT INTO reminders(text, en, type, sttime) VALUES ('Good morning', 1, 1, 1)")
        db.close()
        val dump = ShouterLegacy.dumpFromSqlite(file.readBytes())!!
        assertEquals(1, dump.rules.size)
        assertEquals(1, dump.reminders.size)
        assertEquals(ReminderInterval.DAY, ShouterLegacyMap.reminderMinutes(dump.reminders[0].type))
        file.delete()
    }
}
