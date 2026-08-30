package org.openshouter.privacyreport

import java.nio.charset.StandardCharsets
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SanitizeReportTest {
    private val fixture = loadFixture()

    @Test
    fun nullBecomesEmpty() {
        assertEquals("", SanitizeReport.text(null))
    }

    @Test
    fun redactsSecretsAndHome() {
        val out = SanitizeReport.text(fixture.stack, stack = true)
        for (leak in fixture.mustNotContain) {
            assertFalse(leak, out.contains(leak))
        }
        for (keep in fixture.mustContain) {
            assertTrue(keep, out.contains(keep))
        }
    }

    @Test
    fun fingerprintStableAcrossUsernames() {
        val a = FingerprintCrash.of("Error\n    at C:\\Users\\Ada\\app\\main.ts:1")
        val b = FingerprintCrash.of("Error\n    at C:\\Users\\Bob\\app\\main.ts:1")
        assertEquals(a, b)
        assertEquals(12, a.length)
    }

    @Test
    fun markdownStripsToken() {
        val md = ReportMarkdown.build("crash", "user ghp_abcdefghijklmnopqrstuvwxyz012345 leaked")
        assertFalse(md.contains("ghp_"))
        assertTrue(md.contains("crash"))
    }

    private data class Fixture(
        val stack: String,
        val mustNotContain: List<String>,
        val mustContain: List<String>,
    )

    private fun loadFixture(): Fixture {
        val stream = checkNotNull(
            SanitizeReportTest::class.java.getResourceAsStream("/sanitize-fixtures.json"),
        )
        val obj = JSONObject(stream.use { String(it.readBytes(), StandardCharsets.UTF_8) })
        fun arr(key: String): List<String> {
            val json = obj.getJSONArray(key)
            return (0 until json.length()).map { json.getString(it) }
        }
        return Fixture(obj.getString("stack"), arr("must_not_contain"), arr("must_contain"))
    }
}
