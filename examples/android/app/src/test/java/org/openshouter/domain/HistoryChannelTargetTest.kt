package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryChannelTargetTest {
    @Test
    fun blankPackageIsRejected() {
        assertNull(HistoryChannelTarget.packageOrNull(""))
        assertNull(HistoryChannelTarget.packageOrNull("   "))
        assertEquals("sms.app", HistoryChannelTarget.packageOrNull(" sms.app "))
    }

    @Test
    fun blankChannelHasNoHighlight() {
        assertNull(HistoryChannelTarget.highlightKey(""))
        assertNull(HistoryChannelTarget.highlightKey("  "))
        assertEquals("msg", HistoryChannelTarget.highlightKey(" msg "))
    }
}
