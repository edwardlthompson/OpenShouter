package org.openshouter.silence

/** 100 ms of digital silence as a valid PCM WAV. Empty files make OEM pickers fall back to a ding. */
object SilentWav {
    const val DISPLAY_NAME = "OpenShouter Silent.wav"
    const val TITLE = "OpenShouter Silent"
    const val MIME = "audio/wav"
    private const val SAMPLE_RATE = 8000
    private const val SAMPLES = 800

    fun bytes(): ByteArray {
        val dataSize = SAMPLES * 2
        val out = ByteArray(44 + dataSize)
        ascii(out, 0, "RIFF")
        le32(out, 4, 36 + dataSize)
        ascii(out, 8, "WAVE")
        ascii(out, 12, "fmt ")
        le32(out, 16, 16)
        le16(out, 20, 1)
        le16(out, 22, 1)
        le32(out, 24, SAMPLE_RATE)
        le32(out, 28, SAMPLE_RATE * 2)
        le16(out, 32, 2)
        le16(out, 34, 16)
        ascii(out, 36, "data")
        le32(out, 40, dataSize)
        return out
    }

    fun isSilentUri(uri: String): Boolean {
        val n = uri.trim().lowercase()
        if (n.isEmpty() || n == "null") return true
        return n.contains("openshouter silent") || n.contains("openshouter_silent")
    }

    private fun ascii(out: ByteArray, offset: Int, text: String) {
        text.forEachIndexed { i, c -> out[offset + i] = c.code.toByte() }
    }

    private fun le16(out: ByteArray, offset: Int, value: Int) {
        out[offset] = (value and 0xff).toByte()
        out[offset + 1] = (value shr 8 and 0xff).toByte()
    }

    private fun le32(out: ByteArray, offset: Int, value: Int) {
        le16(out, offset, value)
        le16(out, offset + 2, value shr 16)
    }
}
