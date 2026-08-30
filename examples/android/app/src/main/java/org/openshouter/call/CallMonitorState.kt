package org.openshouter.call

import org.openshouter.data.HistoryDao
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.AppSettings
import org.openshouter.domain.CallPhase
import org.openshouter.domain.IncomingCallEvent
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TelephonyExtras
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

object CallMonitorState {
    suspend fun handleRinging(
        settings: SettingsRepository,
        gate: SpeakGate,
        tts: TtsController,
        history: HistoryDao,
        resolved: String,
        displayName: String,
        sim: String,
        isCallWaiting: Boolean,
        onHistoryLogged: () -> Unit,
    ) {
        val snap = settings.snapshot()
        if (!snap.callsEnabled) return
        if (!gate.allow(snap, ShoutChannel.CALL)) return
        val cleanKey = resolved.ifBlank { displayName }
        if (!CallDedup.shouldAnnounce(cleanKey)) return

        val spoken = if (isCallWaiting && snap.telephonyExtras.callWaitingEnabled) {
            val phrase = TelephonyExtras.callWaiting(displayName.ifBlank { resolved })
            org.openshouter.domain.ChannelStates.spoken(snap, ShoutChannel.CALL, SpokenEvent.Kind.CALL, phrase, looping = false)
        } else {
            val event = IncomingCallEvent(resolved, displayName, CallPhase.RINGING)
            CallChannel.incoming(snap, resolved, event.displayName, sim)
        } ?: return

        tts.speak(spoken)
        onHistoryLogged()
        CallHistory.insertOnce(history, spoken.utterance)
    }

    suspend fun handleHangup(
        snap: AppSettings,
        gate: SpeakGate,
        tts: TtsController,
        history: HistoryDao,
        durationSeconds: Long,
    ) {
        if (!snap.callsEnabled || !snap.telephonyExtras.speakHangupDuration) return
        if (durationSeconds <= 0) return
        if (!gate.allow(snap, ShoutChannel.CALL)) return
        val durText = TelephonyExtras.formatDuration(durationSeconds)
        val phrase = "Call ended, duration $durText"
        val spoken = org.openshouter.domain.ChannelStates.spoken(snap, ShoutChannel.CALL, SpokenEvent.Kind.CALL, phrase, looping = false)
        tts.speak(spoken)
        CallHistory.insertOnce(history, phrase)
    }

    suspend fun handleMissed(
        snap: AppSettings,
        gate: SpeakGate,
        tts: TtsController,
        history: HistoryDao,
        contacts: org.openshouter.contacts.ContactsLookup,
        number: String,
    ) {
        val spoken = CallChannel.missed(snap, number, contacts.nameFor(number)) ?: return
        if (!gate.allow(snap, ShoutChannel.CALL)) return
        tts.speak(spoken)
        CallHistory.insertOnce(history, spoken.utterance)
    }
}
