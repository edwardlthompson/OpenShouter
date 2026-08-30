package org.openshouter.history

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.openshouter.data.HistoryEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class HistoryAndBackupSprint31Test {

    @Test
    fun exportSanitizedJsonBytes() {
        val rows = listOf(
            HistoryEntity(
                id = 1L,
                postedAt = 1700000000000L,
                packageName = "org.example.app",
                title = "Private Title",
                text = "Secret message content",
                spoken = "Announcement",
                ignoreReason = "NONE",
                channelName = "General",
            ),
        )

        val bytes = HistoryExport.exportToJsonBytes(rows)
        val jsonString = String(bytes, Charsets.UTF_8)
        val jsonArray = JSONArray(jsonString)

        assertEquals(1, jsonArray.length())
        val item = jsonArray.getJSONObject(0)
        assertEquals(1700000000000L, item.getLong("postedAt"))
        assertEquals("org.example.app", item.getString("packageName"))
        assertEquals("NONE", item.getString("ignoreReason"))
        // Confirms raw title/text are not exported to preserve privacy
        assertTrue(!item.has("title"))
        assertTrue(!item.has("text"))
    }

    @Test
    fun retentionCutoffCalculations() {
        val now = 100_000_000_000L
        val sevenDaysMs = 7L * 24L * 60L * 60L * 1000L
        assertEquals(now - sevenDaysMs, HistoryExport.retentionCutoffMs(now, 7))

        val thirtyDaysMs = 30L * 24L * 60L * 60L * 1000L
        assertEquals(now - thirtyDaysMs, HistoryExport.retentionCutoffMs(now, 30))

        val ninetyDaysMs = 90L * 24L * 60L * 60L * 1000L
        assertEquals(now - ninetyDaysMs, HistoryExport.retentionCutoffMs(now, 90))
    }
}
