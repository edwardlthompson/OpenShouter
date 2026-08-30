package dev.foss.goldenpath.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DistributionSprint33Test {

    @Test
    fun whatsNewVisibilityLogic() {
        assertFalse(WhatsNew.shouldShow(lastSeenVersionCode = 0, currentVersionCode = 29))
        assertFalse(WhatsNew.shouldShow(lastSeenVersionCode = 29, currentVersionCode = 29))
        assertTrue(WhatsNew.shouldShow(lastSeenVersionCode = 28, currentVersionCode = 29))
        assertTrue(WhatsNew.highlightsForVersion(29).isNotEmpty())
    }

    @Test
    fun unifiedPushConfiguration() {
        val disabled = UnifiedPushPing.PushConfig(enabled = false, endpoint = "https://push.example.com")
        assertFalse(UnifiedPushPing.isConfigured(disabled))

        val blankEndpoint = UnifiedPushPing.PushConfig(enabled = true, endpoint = "")
        assertFalse(UnifiedPushPing.isConfigured(blankEndpoint))

        val active = UnifiedPushPing.PushConfig(enabled = true, endpoint = "https://push.example.com/endpoint")
        assertTrue(UnifiedPushPing.isConfigured(active))
    }
}
