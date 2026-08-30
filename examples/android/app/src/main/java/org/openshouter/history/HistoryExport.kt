package org.openshouter.history

import java.nio.charset.StandardCharsets
import org.json.JSONArray
import org.json.JSONObject
import org.openshouter.data.HistoryEntity

object HistoryExport {
    fun exportToJsonBytes(rows: List<HistoryEntity>): ByteArray {
        val array = JSONArray()
        rows.forEach { row ->
            val obj = JSONObject().apply {
                put("postedAt", row.postedAt)
                put("packageName", row.packageName)
                put("kind", row.kind)
                put("ignoreReason", row.ignoreReason)
                put("channelName", row.channelName)
            }
            array.put(obj)
        }
        return array.toString(2).toByteArray(StandardCharsets.UTF_8)
    }

    fun retentionCutoffMs(nowMs: Long, days: Int): Long =
        nowMs - (days.toLong() * 24L * 60L * 60L * 1000L)
}
