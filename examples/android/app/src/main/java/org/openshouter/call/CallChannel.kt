package org.openshouter.call

import org.openshouter.contacts.ContactRules
import org.openshouter.domain.AppSettings
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsFormat

object CallChannel {
    fun incoming(settings: AppSettings, rawNumber: String, contactName: String?): SpokenEvent? {
        val resolved = ContactRules.apply(settings.contactRule, rawNumber, contactName)
        if (resolved.blocked) return null
        if (!resolved.known && !settings.missedCall.speakUnknown) return null
        return ChannelStates.spoken(
            settings, ShoutChannel.CALL, SpokenEvent.Kind.CALL,
            TtsFormat.call(settings.callFormat, resolved.spoken), looping = true,
        )
    }

    fun missed(settings: AppSettings, rawNumber: String, contactName: String?): SpokenEvent? {
        if (!ContactRules.shouldSpeakCall(settings.contactRule, settings.missedCall, rawNumber, contactName)) {
            return null
        }
        val spoken = ContactRules.apply(settings.contactRule, rawNumber, contactName).spoken
        return ChannelStates.spoken(
            settings, ShoutChannel.CALL, SpokenEvent.Kind.CALL,
            TtsFormat.call(TtsFormat.MISSED_DEFAULT, spoken),
        )
    }
}
