package org.openshouter.backup

import java.io.ByteArrayOutputStream
import java.io.InputStream

object BackupLimits {
    const val MAX_ZIP_BYTES = 2_000_000
    const val MAX_ENTRY_BYTES = 1_000_000
    const val MAX_RULES = 500

    fun readBounded(stream: InputStream, max: Int): ByteArray? {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(8192)
        var total = 0
        while (true) {
            val n = stream.read(buf)
            if (n <= 0) break
            total += n
            if (total > max) return null
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }
}
