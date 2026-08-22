package org.openshouter.backup

import org.openshouter.domain.BatteryPhrases
import org.openshouter.domain.BatterySituation
import org.openshouter.domain.TtsFormat
import org.openshouter.domain.TtsVoice

internal object ShouterLegacyPhrases {
    fun affix(prefix: String?, suffix: String?, fallback: String, token: String): String {
        val parts = listOf(prefix.orEmpty().trim(), token, suffix.orEmpty().trim()).filter { it.isNotEmpty() }
        return parts.joinToString(" ").ifBlank { fallback }
    }

    fun notif(p: Map<String, String>): String {
        val pre = p["Enotifprefix"].orEmpty().trim()
        val suf = p["Enotifsuffix"].orEmpty().trim()
        if (pre.isEmpty() && suf.isEmpty()) return TtsFormat.DEFAULT
        return listOf(pre, "%app", suf, "%text").filter { it.isNotEmpty() }.joinToString(" ")
    }

    fun langTag(raw: String?): String {
        val tag = raw.orEmpty().trim()
        if (tag.isEmpty() || tag == "0" || tag.all { it.isDigit() }) return ""
        return tag.take(TtsVoice.MAX_TAG)
    }

    fun battery(p: Map<String, String>): BatteryPhrases {
        val on = flag(p, "enabbatrysht")
        val enabled = buildSet {
            if (on && flag(p, "enbatshtf_3")) add(BatterySituation.LOW)
            if (on && flag(p, "enbatshtf_5")) add(BatterySituation.FULL)
            if (on && flag(p, "enbatshtf_1")) add(BatterySituation.CONNECTED)
            if (on && flag(p, "enbatshtf_2")) add(BatterySituation.DISCONNECTED)
            if (on && flag(p, "enbatshtf_4")) add(BatterySituation.LEVEL)
        }
        return BatteryPhrases(
            enabled = if (on) enabled else emptySet(),
            low = p["enbtshtmsgf_3"]?.take(80) ?: BatteryPhrases.DEFAULT_LOW,
            full = p["enbtshtmsgf_5"]?.take(80) ?: BatteryPhrases.DEFAULT_FULL,
            connected = p["enbtshtmsgf_1"]?.take(80) ?: BatteryPhrases.DEFAULT_CONNECTED,
            disconnected = p["enbtshtmsgf_2"]?.take(80) ?: BatteryPhrases.DEFAULT_DISCONNECTED,
            level = affix(p["enbtshtmsgf_su"], p["enbtshtmsgf_pr"], BatteryPhrases.DEFAULT_LEVEL, "%level"),
        )
    }

    private fun flag(p: Map<String, String>, key: String): Boolean =
        ShouterLegacyParse.shoutEnabled(p[key].orEmpty())
}
