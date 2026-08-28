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
    }
}
