package org.openshouter.call

object CallDedup {
    private const val DEDUP_WINDOW_MS = 6000L
    @Volatile private var lastAnnouncedKey: String? = null
    @Volatile private var lastAnnouncedAt: Long = 0L

    fun shouldAnnounce(key: String, now: Long = System.currentTimeMillis()): Boolean {
        val cleanKey = key.trim().lowercase()
        if (cleanKey.isEmpty()) return true
        if (cleanKey == lastAnnouncedKey && (now - lastAnnouncedAt) < DEDUP_WINDOW_MS) {
            return false
        }
        lastAnnouncedKey = cleanKey
        lastAnnouncedAt = now
        return true
    }

    fun clear() {
        lastAnnouncedKey = null
        lastAnnouncedAt = 0L
    }
}
