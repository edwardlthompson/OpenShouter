package org.openshouter.i18n

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class I18nEsTest {
    @Test
    fun spanishKeysMatchEnglish() {
        assertLocaleParity("src/main/res/values-es/strings.xml")
    }

    @Test
    fun frenchKeysMatchEnglish() {
        assertLocaleParity("src/main/res/values-fr/strings.xml")
    }

    private fun assertLocaleParity(rel: String) {
        val en = keysIn("src/main/res/values")
        val loc = keysIn(rel.substringBeforeLast('/'))
        assertTrue("English pack is empty", en.isNotEmpty())
        assertEquals(emptySet<String>(), en - loc)
        assertEquals(emptySet<String>(), loc - en)
    }

    private fun keysIn(relDir: String): Set<String> {
        val dir = locate(relDir)
        return dir.listFiles { f -> f.name.startsWith("strings") && f.name.endsWith(".xml") }
            .orEmpty()
            .flatMap { file ->
                Regex("""<string\s+name="([^"]+)"""").findAll(file.readText(Charsets.UTF_8))
                    .map { it.groupValues[1] }
            }
            .toSet()
    }

    private fun locate(rel: String): File {
        val cwd = File(System.getProperty("user.dir"))
        val candidates = listOf(
            cwd.resolve(rel),
            cwd.resolve("app").resolve(rel),
            cwd.resolve("examples/android/app").resolve(rel),
        )
        return candidates.first { it.exists() }
    }
}
