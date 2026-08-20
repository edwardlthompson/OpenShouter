package org.openshouter.i18n

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class I18nEsTest {
    @Test
    fun spanishKeysMatchEnglish() {
        val en = keys(locate("src/main/res/values/strings.xml"))
        val es = keys(locate("src/main/res/values-es/strings.xml"))
        assertTrue("English pack is empty", en.isNotEmpty())
        assertEquals(emptySet<String>(), en - es)
        assertEquals(emptySet<String>(), es - en)
    }

    private fun keys(file: File): Set<String> {
        val text = file.readText(Charsets.UTF_8)
        return Regex("""<string\s+name="([^"]+)"""").findAll(text).map { it.groupValues[1] }.toSet()
    }

    private fun locate(rel: String): File {
        val cwd = File(System.getProperty("user.dir"))
        val candidates = listOf(
            cwd.resolve(rel),
            cwd.resolve("app").resolve(rel),
            cwd.resolve("examples/android/app").resolve(rel),
        )
        return candidates.first { it.isFile }
    }
}
