package org.openshouter.intent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class FossIntegrationsSprint34Test {

    @Test
    fun publicShoutIntentSanitization() {
        assertEquals("Hello world", PublicShoutIntent.sanitizeText("  Hello world  "))
        assertNull(PublicShoutIntent.sanitizeText(""))
        assertNull(PublicShoutIntent.sanitizeText("   "))
        assertNull(PublicShoutIntent.sanitizeText(null))
    }

    @Test
    fun taskerPluginBundle() {
        val bundle = TaskerPlugin.buildBundle(enabled = true)
        assertTrue(TaskerPlugin.isEnabled(bundle))
    }
}
