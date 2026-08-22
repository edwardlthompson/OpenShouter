package org.openshouter.backup

import android.database.sqlite.SQLiteDatabase
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.TtsStream
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ShouterLegacyRootTest {
    @Test
    fun prefsMapSpeakAppCallSuffixAndChannels() {
        val xml = """
            <map>
              <boolean name="Enotifrdapname" value="false" />
              <boolean name="enremasht" value="false" />
              <string name="Ecllerprefix">Call from</string>
              <string name="Ecllersuffix">now</string>
              <string name="Enotifstrm">5</string>
              <string name="Ecllerrptcnt">8</string>
              <string name="Emsgprefix">You got a message from</string>
              <string name="entmforid">1</string>
              <boolean name="enabbatrysht" value="true" />
              <boolean name="enbatshtf_4" value="true" />
              <string name="enbtshtmsgf_su">Battery is</string>
              <string name="enbtshtmsgf_pr">left</string>
              <boolean name="Ennotionsilentonly" value="true" />
              <string name="pk_setting_tts_lang">0</string>
            </map>
        """.trimIndent().toByteArray()
        val prefs = ShouterLegacy.prefsFromXml(xml)!!
        val mapped = ShouterLegacyMap.map(LegacyDump(prefs = prefs))
        assertFalse(mapped.speakApp)
        assertFalse(mapped.remindersOn)
        assertTrue(mapped.callFormat.contains("now"))
        assertEquals("", mapped.playback.voice.languageTag)
        assertTrue(mapped.messageFormat.contains("%name"))
        assertEquals(org.openshouter.domain.TimeHourStyle.HOUR_12, mapped.timeHour)
        assertTrue(org.openshouter.domain.BatterySituation.LEVEL in mapped.battery.enabled)
        assertTrue(mapped.battery.level.contains("%level"))
        val states = ShouterLegacyChannels.states(prefs)
        assertEquals(TtsStream.NOTIFICATION, states.getValue(ShoutChannel.NOTIFICATION).stream)
        assertEquals(0, states.getValue(ShoutChannel.CALL).repeatCount)
        assertTrue(states.getValue(ShoutChannel.NOTIFICATION).device.allowSilentVibrate)
    }

    @Test
    fun rootDumpReadsSqliteAndPrefs() {
        val file = File.createTempFile("shdb", ".db")
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        db.execSQL("CREATE TABLE apps(package TEXT PRIMARY KEY, shout TEXT)")
        db.execSQL("INSERT INTO apps(package, shout) VALUES ('com.enabled.app', '1')")
        db.close()
        val sqlite = file.readBytes()
        file.delete()
        val xml = """<map><boolean name="Entimeshout" value="true" /></map>""".toByteArray()
        val dump = ShouterLegacyRoot.readDump { cmd ->
            when {
                cmd.startsWith("id") -> "uid=0(root)".toByteArray()
                cmd.contains("proshouter") && cmd.contains("shdb") -> sqlite
                cmd.contains("proshouter") && cmd.contains("preferences.xml") -> xml
                else -> null
            }
        }
        assertTrue(ShouterLegacyRoot.available { "uid=0(root)".toByteArray() })
        assertEquals(1, dump.rules.size)
        assertTrue(dump.prefs.containsKey("Entimeshout"))
    }
}
