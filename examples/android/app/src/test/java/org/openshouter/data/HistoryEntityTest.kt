package org.openshouter.data

import org.junit.Assert.assertEquals
import org.junit.Test
import org.openshouter.domain.IgnoreReason

class HistoryEntityTest {
    @Test
    fun channelFieldsDefaultBlank() {
        val row = HistoryEntity(
            postedAt = 1L,
            packageName = "sms.app",
            title = "",
            text = "",
            spoken = "",
        )
        assertEquals(IgnoreReason.NONE.name, row.ignoreReason)
        assertEquals("", row.channelId)
        assertEquals("", row.channelName)
        assertEquals("NOTIFICATION", row.kind)
    }

    @Test
    fun cellularCallRowHasEmptyTitleAndCallKind() {
        val row = org.openshouter.call.CallHistory.ringingRow(9L, "Incoming call from Ada")
        assertEquals("com.android.phone", row.packageName)
        assertEquals("", row.title)
        assertEquals("", row.text)
        assertEquals("CALL", row.kind)
        assertEquals("Incoming call from Ada", row.spoken)
    }
}
