package org.openshouter.call

import org.openshouter.contacts.ContactRules
import org.openshouter.domain.ContactRule
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsFormat

object CallChannel {
    fun incoming(
        settings: AppSettings,
        rawNumber: String,
        contactName: String?,
        sim: String = "",
        appLabel: String = "",
        looping: Boolean = true,
        repeatCount: Int? = null,
    ): SpokenEvent? {
        val resolved = ContactRules.apply(settings.contactRule, rawNumber, contactName)
        if (resolved.blocked) return null
        val name = when {
            resolved.known -> resolved.spoken
            appLabel.isNotBlank() -> appLabel
            !settings.missedCall.speakUnknown -> return null
            else -> resolved.spoken
        }
        val isConf = settings.telephonyExtras.conferenceHintEnabled &&
            (name.contains("conference", ignoreCase = true) || name.contains("participants", ignoreCase = true))
        val template = if (isConf) {
            "Conference call from %name"
        } else if (appLabel.isNotBlank() && resolved.known && "%app" !in settings.callFormat) {
            "Incoming %app call from %name"
        } else {
            settings.callFormat
        }
        return ChannelStates.spoken(
            settings, ShoutChannel.CALL, SpokenEvent.Kind.CALL,
            TtsFormat.call(
                template,
                name,
                ContactRule.speakableNumber(rawNumber),
                sim,
                appLabel,
            ),
            looping = looping,
            repeatCount = repeatCount,
        )
    }

    fun missed(settings: AppSettings, rawNumber: String, contactName: String?): SpokenEvent? {
        if (!ContactRules.shouldSpeakCall(settings.contactRule, settings.missedCall, rawNumber, contactName)) {
            return null
        }
        val spoken = ContactRules.apply(settings.contactRule, rawNumber, contactName).spoken
        return ChannelStates.spoken(
            settings, ShoutChannel.CALL, SpokenEvent.Kind.CALL,
            TtsFormat.call(TtsFormat.MISSED_DEFAULT, spoken, ContactRule.speakableNumber(rawNumber)),
        )
    }
}
