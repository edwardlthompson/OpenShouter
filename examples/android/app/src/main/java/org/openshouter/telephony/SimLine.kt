package org.openshouter.telephony

import android.content.Context
import android.content.Intent
import android.telephony.SubscriptionManager

object SimLine {
    fun spoken(displayName: String?): String = displayName?.trim().orEmpty()

    fun pick(names: List<String>, matched: String?): String {
        val clean = names.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isEmpty()) return ""
        if (clean.size == 1) return clean[0]
        return spoken(matched)
    }

    fun resolve(context: Context, intent: Intent?): String = runCatching {
        val manager = context.getSystemService(SubscriptionManager::class.java) ?: return ""
        val infos = manager.activeSubscriptionInfoList ?: emptyList()
        val names = infos.map { it.displayName?.toString().orEmpty() }
        val subId = extraInt(
            intent,
            "subscription",
            "subscriptionId",
            "android.telephony.extra.SUBSCRIPTION_INDEX",
        )
        val slot = extraInt(
            intent,
            "slot",
            "slotIndex",
            "android.telephony.extra.SLOT_INDEX",
        )
        val matched = when {
            subId != null -> infos.firstOrNull { it.subscriptionId == subId }?.displayName?.toString()
            slot != null -> infos.firstOrNull { it.simSlotIndex == slot }?.displayName?.toString()
            else -> null
        }
        pick(names, matched)
    }.getOrDefault("")

    private fun extraInt(intent: Intent?, vararg keys: String): Int? {
        if (intent == null) return null
        for (key in keys) {
            val value = intent.getIntExtra(key, Int.MIN_VALUE)
            if (value != Int.MIN_VALUE && value >= 0) return value
        }
        return null
    }
}
