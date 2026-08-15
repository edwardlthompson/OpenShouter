package org.openshouter.contacts

import org.openshouter.domain.ContactRule
import org.openshouter.domain.MessageChannelPolicy
import org.openshouter.domain.MissedCallPolicy

data class ResolvedContact(
    val blocked: Boolean,
    val spoken: String,
    val known: Boolean,
) {
    override fun toString(): String = "ResolvedContact(blocked=$blocked, known=$known)"
}

object ContactRules {
    fun apply(rule: ContactRule, rawNumber: String, contactName: String?): ResolvedContact {
        val name = contactName.orEmpty().trim()
        val key = ContactRule.normalize(rawNumber)
        val nick = if (key.isEmpty()) "" else rule.nicknames[key].orEmpty().trim()
        val known = name.isNotEmpty() || nick.isNotEmpty()
        if (rule.isBlocked(rawNumber)) {
            return ResolvedContact(blocked = true, spoken = "", known = known)
        }
        return ResolvedContact(blocked = false, spoken = rule.display(rawNumber, name), known = known)
    }

    fun resolve(rule: ContactRule, rawNumber: String, lookup: ContactsLookup): ResolvedContact =
        apply(rule, rawNumber, lookup.nameFor(rawNumber))

    fun addNick(rule: ContactRule, rawNumber: String, display: String): ContactRule? {
        val key = ContactRule.normalize(rawNumber)
        val nick = display.trim().take(ContactRule.MAX_NICK)
        if (key.isEmpty() || nick.isEmpty()) return null
        if (key !in rule.nicknames && rule.nicknames.size >= ContactRule.MAX_RULES) return null
        return rule.copy(nicknames = rule.nicknames + (key to nick))
    }

    fun addBlock(rule: ContactRule, rawNumber: String): ContactRule? {
        val key = ContactRule.normalize(rawNumber)
        if (key.isEmpty()) return null
        if (key !in rule.blacklist && rule.blacklist.size >= ContactRule.MAX_RULES) return null
        return rule.copy(blacklist = rule.blacklist + key)
    }

    fun shouldSpeakCall(
        rule: ContactRule,
        policy: MissedCallPolicy,
        rawNumber: String,
        contactName: String?,
    ): Boolean {
        val resolved = apply(rule, rawNumber, contactName)
        return !resolved.blocked && policy.allows(resolved.known)
    }

    fun shouldSpeakMessage(
        rule: ContactRule,
        policy: MessageChannelPolicy,
        rawNumber: String,
        contactName: String?,
    ): Boolean {
        val resolved = apply(rule, rawNumber, contactName)
        return !resolved.blocked && policy.allows(resolved.known)
    }
}
