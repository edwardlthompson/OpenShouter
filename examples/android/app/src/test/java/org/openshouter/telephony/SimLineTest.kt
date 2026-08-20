package org.openshouter.telephony

import org.junit.Assert.assertEquals
import org.junit.Test
import org.openshouter.domain.TtsFormat

class SimLineTest {
    @Test
    fun oneLineUsesNameUnknownStaysBlank() {
        assertEquals("", SimLine.spoken("  "))
        assertEquals("Work", SimLine.spoken(" Work "))
        assertEquals("Work", SimLine.pick(listOf("Work"), null))
        assertEquals("Work", SimLine.pick(listOf("Personal", "Work"), "Work"))
        assertEquals("", SimLine.pick(listOf("Personal", "Work"), null))
        assertEquals("", SimLine.pick(emptyList(), "Work"))
        assertEquals(
            "Incoming call from Pat on Work",
            TtsFormat.call("Incoming call from %name on %sim", "Pat", "", "Work"),
        )
        assertEquals(
            "Incoming call from Pat",
            TtsFormat.call("Incoming call from %name on %sim", "Pat", "", ""),
        )
    }
}
