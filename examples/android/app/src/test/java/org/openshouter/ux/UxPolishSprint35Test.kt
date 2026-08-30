package org.openshouter.ux

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openshouter.domain.QuietHours
import org.openshouter.ui.channel.FormatPreview

class UxPolishSprint35Test {

    @Test
    fun formatPreviewRendering() {
        val format = "%app: %name says %text at %time"
        val rendered = FormatPreview.render(
            format = format,
            sampleApp = "Messages",
            sampleSender = "Bob",
            sampleText = "Hello there",
            sampleTime = "12:00 PM",
        )
        assertEquals("Messages: Bob says Hello there at 12:00 PM", rendered)
    }

    @Test
    fun quietHoursTransitionMinutes() {
        assertEquals(60, QuietHours.minutesUntil(10 * 60, 11 * 60))
        assertEquals(120, QuietHours.minutesUntil(23 * 60, 1 * 60))
        assertEquals(0, QuietHours.minutesUntil(8 * 60, 8 * 60))
    }
}
