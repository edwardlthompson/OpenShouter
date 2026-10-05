package org.openshouter.call

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openshouter.contacts.ContactsLookup
import org.openshouter.data.HistoryDao
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.CallPhase
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

internal object CallMonitorRing {
    fun launch(
        scope: CoroutineScope,
        lookup: CallLogLookup,
        contacts: ContactsLookup,
        settings: SettingsRepository,
        gate: SpeakGate,
        tts: TtsController,
        history: HistoryDao,
        number: String,
        sim: String,
        isCallWaiting: Boolean,
        startedGen: Long,
        phase: () -> CallPhase,
        generation: () -> Long,
        lastNumber: () -> String,
        setNumber: (String) -> Unit,
        setSim: (String) -> Unit,
        currentSim: () -> String,
        onHistoryLogged: () -> Unit,
    ) {
        scope.launch {
            var resolved = lookup.resolve(number)
            if (resolved.isBlank()) {
                delay(400)
                resolved = lookup.resolve(number)
            }
            if (!CallRingGate.shouldContinueRingWork(phase(), generation(), startedGen)) return@launch
            if (phase() == CallPhase.RINGING && lastNumber().isNotBlank()) {
                if (resolved.isBlank() || resolved == lastNumber()) return@launch
            }
            if (!CallRingGate.shouldContinueRingWork(phase(), generation(), startedGen)) return@launch
            setNumber(resolved)
            setSim(sim)
            val displayName = contacts.nameFor(resolved).orEmpty()
            if (!CallRingGate.shouldContinueRingWork(phase(), generation(), startedGen)) return@launch
            CallMonitorState.handleRinging(
                settings, gate, tts, history, resolved, displayName, currentSim(), isCallWaiting, onHistoryLogged,
            )
        }
    }
}
