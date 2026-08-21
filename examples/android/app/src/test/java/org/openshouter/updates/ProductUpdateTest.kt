package org.openshouter.updates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductUpdateTest {

    @Test
    fun dailyCheckWaitsAFullDay() {
        assertTrue(ProductUpdate.shouldCheckDaily(null, 0L))
        assertFalse(ProductUpdate.shouldCheckDaily(0L, ProductUpdate.MS_DAY - 1))
        assertTrue(ProductUpdate.shouldCheckDaily(0L, ProductUpdate.MS_DAY))
    }

    @Test
    fun apkVersionIgnoresTemplateTags() {
        assertEquals("0.3.1", ProductUpdate.parseApkVersion("openshouter-0.3.1-foss.apk"))
        assertEquals(null, ProductUpdate.parseApkVersion("v0.22.1"))
        assertEquals(null, ProductUpdate.parseApkVersion("sbom.cyclonedx.json"))
    }

    @Test
    fun isNewerThanCurrentInstaller() {
        assertTrue(ProductUpdate.isNewerVersion("0.3.0", "0.3.1"))
        assertFalse(ProductUpdate.isNewerVersion("0.3.1", "0.3.1"))
        assertFalse(ProductUpdate.isNewerVersion("0.4.0", "0.3.1"))
    }

    @Test
    fun donateNudgeOnlyAfterVersionChange() {
        assertFalse(ProductUpdate.shouldNudgeDonate(null, "0.3.0"))
        assertFalse(ProductUpdate.shouldNudgeDonate("0.3.0", "0.3.0"))
        assertTrue(ProductUpdate.shouldNudgeDonate("0.3.0", "0.3.1"))
    }

    @Test
    fun selectApkAssetReadsFossFilename() {
        val picked = ProductUpdate.selectApkAsset(
            listOf(
                ProductUpdate.NamedAsset("sbom.cyclonedx.json", "https://example.com/sbom"),
                ProductUpdate.NamedAsset(
                    "openshouter-0.3.1-foss.apk",
                    "https://example.com/a.apk",
                ),
            ),
        )
        assertEquals("0.3.1", picked?.version)
        assertEquals("https://example.com/a.apk", picked?.url)
    }

    @Test
    fun updatePromptSkipsDismissedVersion() {
        assertTrue(ProductUpdate.shouldPromptUpdate("0.3.0", "0.3.1", null))
        assertFalse(ProductUpdate.shouldPromptUpdate("0.3.0", "0.3.1", "0.3.1"))
        assertFalse(ProductUpdate.shouldPromptUpdate("0.3.1", "0.3.1", null))
    }

}
