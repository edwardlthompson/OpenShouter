package org.openshouter.domain

data class ContactRule(
    val nicknames: Map<String, String> = emptyMap(),
    val blacklist: Set<String> = emptySet(),
) {
    fun isBlocked(raw: String): Boolean {
        val key = normalize(raw)
        return key.isNotEmpty() && key in blacklist
    }

    fun display(raw: String, contactName: String, unknown: String = UNKNOWN): String {
        val key = normalize(raw)
        nicknames[key]?.trim()?.takeIf { it.isNotEmpty() }?.let { return it.take(MAX_NICK) }
        contactName.trim().takeIf { it.isNotEmpty() }?.let { return it }
        return unknown
    }

    override fun toString(): String =
        "ContactRule(nicks=${nicknames.size}, blocked=${blacklist.size})"

    companion object {
        const val UNKNOWN = "unknown"
        const val MAX_NICK = 40
        const val MAX_RULES = 200

        fun normalize(raw: String): String = raw.filter { it.isDigit() }.takeLast(10)

        fun parse(stored: Set<String>): ContactRule {
            val nicks = linkedMapOf<String, String>()
            val blocked = linkedSetOf<String>()
            for (row in stored) {
                when {
                    row.startsWith("nick:") -> {
                        val body = row.removePrefix("nick:")
                        val idx = body.indexOf('=')
                        if (idx <= 0) continue
                        val key = normalize(body.substring(0, idx))
                        val nick = body.substring(idx + 1).trim().take(MAX_NICK)
                        if (key.isNotEmpty() && nick.isNotEmpty() && nicks.size < MAX_RULES) {
                            nicks[key] = nick
                        }
                    }
                    row.startsWith("block:") -> {
                        val key = normalize(row.removePrefix("block:"))
                        if (key.isNotEmpty() && blocked.size < MAX_RULES) blocked.add(key)
                    }
                }
            }
            return ContactRule(nicks, blocked)
        }

        fun encode(rule: ContactRule): Set<String> {
            val out = linkedSetOf<String>()
            rule.nicknames.entries.take(MAX_RULES).forEach { (k, v) ->
                val key = normalize(k)
                val nick = v.trim().take(MAX_NICK)
                if (key.isNotEmpty() && nick.isNotEmpty()) out.add("nick:$key=$nick")
            }
            rule.blacklist.take(MAX_RULES).forEach { raw ->
                val key = normalize(raw)
                if (key.isNotEmpty()) out.add("block:$key")
            }
            return out
        }
    }
}
