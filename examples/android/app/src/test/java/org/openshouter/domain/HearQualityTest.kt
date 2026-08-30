package org.openshouter.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HearQualityTest {
    @Test
    fun appOverrideFormatAndImportance() {
        val override = AppOverride(
            packageName = "com.chat",
            format = "%title says %text",
            minImportance = SpeakImportance.HIGH,
            appNameCooldownSeconds = 120,
        )
        val encoded = AppOverrides.encodeFull(mapOf(override.packageName to override))
        val parsed = AppOverrides.parseFull(encoded)

        val restored = parsed["com.chat"]
        assertEquals("%title says %text", restored?.format)
        assertEquals(SpeakImportance.HIGH, restored?.minImportance)
        assertEquals(120, restored?.appNameCooldownSeconds)
        assertEquals("%title says %text", restored?.mergeFormat("%app: %text"))
    }

    @Test
    fun notificationPolicyFiltersBubblesAndWorkProfile() {
        val policy = NotificationPolicy(
            ignoreBubbles = true,
            ignoreWorkProfile = true,
            contactCooldownSeconds = 45,
        )
        assertEquals(true, policy.ignoreBubbles)
        assertEquals(true, policy.ignoreWorkProfile)
        assertEquals(45, policy.contactCooldownSeconds)
    }

    @Test
    fun contactCooldownCalculation() {
        val now = 100_000L
        val lastAt = 80_000L
        // 20s difference, 30s cooldown -> should block (false)
        assertEquals(false, AppNameCooldown.allow(lastAt, now, 30))
        // 20s difference, 10s cooldown -> should allow (true)
        assertEquals(true, AppNameCooldown.allow(lastAt, now, 10))
    }
}
