package org.openshouter.call

import org.openshouter.domain.AppSettings
import org.openshouter.domain.CallRepeatModes
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent

object CallPosted {
    fun action(
        packageName: String,
        notificationKey: String,
        categoryCall: Boolean,
        isOngoing: Boolean,
        callType: Int?,
        session: CallAnnounceSession,
    ): CallAnnounceAction {
        val phase = VoipCallPhaseLogic.phase(categoryCall, isOngoing, callType)
        return session.decide(packageName, notificationKey, phase)
    }

    fun eventFor(
        settings: AppSettings,
        title: String,
        people: String,
        appLabel: String,
        packageName: String,
    ): SpokenEvent? {
        val mode = CallRepeatModes.modeFor(packageName, settings.callRepeatModes)
        if (!CallRepeatModes.shouldSpeak(mode)) return null
        val channelRepeat = ChannelStates.resolve(
            settings.channelStates,
            ShoutChannel.CALL,
            settings.deviceState,
            settings.ttsPlayback,
        ).repeatCount
        return CallNotification.event(
            settings,
            title,
            people,
            appLabel,
            looping = CallRepeatModes.looping(mode),
            repeatCount = CallRepeatModes.spokenRepeatCount(mode, channelRepeat),
        )
    }
}
