package org.openshouter.domain

object CallNumberResolver {
    fun prefer(hint: String, fromLog: String?): String {
        val trimmed = hint.trim()
        if (trimmed.isNotBlank()) return trimmed
        return fromLog?.trim().orEmpty()
    }
}
