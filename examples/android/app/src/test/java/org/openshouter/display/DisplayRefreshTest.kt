package org.openshouter.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayRefreshTest {
    @Test
    fun picksFastestModeAtCurrentResolution() {
        val current = DisplayModeChoice(1, 1080, 2400, 60f)
        val modes = listOf(
            current,
            DisplayModeChoice(2, 1080, 2400, 120f),
            DisplayModeChoice(3, 1440, 3200, 144f),
            DisplayModeChoice(4, 1080, 2400, 90f),
        )
        assertEquals(2, fastestSameResolutionModeId(modes, current))
    }

    @Test
    fun emptyModesReturnsNull() {
        val current = DisplayModeChoice(1, 1080, 2400, 60f)
        assertNull(fastestSameResolutionModeId(emptyList(), current))
    }
}
