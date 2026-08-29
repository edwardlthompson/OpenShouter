package org.openshouter.message

import org.openshouter.contacts.ContactRules
import org.openshouter.domain.AppNameCooldown
import org.openshouter.domain.AppSettings
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsFormat

data class MessageParse(
    val sender: String,
    val body: String,
) {
    override fun toString(): String = "MessageParse"
}

object MessageChannel {
    fun isMessaging(packageName: String): Boolean {
        val pkg = packageName.lowercase()
        return pkg.contains("sms") ||
            pkg.contains("mms") ||
            pkg.contains("messaging") ||
            pkg.contains("whatsapp") ||
            pkg.contains("telegram") ||
            pkg.contains("signal")
    }

    fun parse(title: String, text: String, people: String = ""): MessageParse {
        val sender = title.trim().ifEmpty { people.trim() }
        return MessageParse(sender = sender, body = text.trim())
    }

    fun utterance(
        settings: AppSettings,
        parsed: MessageParse,
        contactName: String?,
        appLabel: String = "",
        includeAppName: Boolean = true,
    ): String? {
        val policy = settings.messageChannel
        if (!policy.enabled) return null
        val resolved = ContactRules.apply(settings.contactRule, parsed.sender, contactName)
        if (resolved.blocked || !policy.allows(resolved.known)) return null
        val body = if (policy.speakBody) parsed.body else ""
        val name = if (!includeAppName && AppNameCooldown.isAppLabel(parsed.sender, appLabel)) {
            ""
        } else {
            resolved.spoken
        }
        if (name.isBlank() && !includeAppName) {
            return body.takeIf { it.isNotBlank() }
        }
        val spoken = TtsFormat.message(settings.messageFormat, name, body)
        return spoken.takeIf { it.isNotBlank() }
    }

    fun event(settings: AppSettings, parsed: MessageParse, contactName: String?): SpokenEvent? {
        val text = utterance(settings, parsed, contactName) ?: return null
        return SpokenEvent(SpokenEvent.Kind.MESSAGE, text)
    }
}
