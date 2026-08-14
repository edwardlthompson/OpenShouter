package org.openshouter.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openshouter.contacts.ContactsLookup
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.CallPhase
import org.openshouter.domain.IncomingCallEvent
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TtsFormat
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

@Singleton
class CallMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val contacts: ContactsLookup,
    private val lookup: CallLogLookup,
    private val tts: TtsController,
    private val gate: SpeakGate,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    @Volatile private var started = false
    @Volatile private var lastPhase = CallPhase.IDLE
    @Volatile private var lastNumber = ""

    private val phoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
            val extra = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER).orEmpty()
            val state = when (extra) {
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                else -> TelephonyManager.CALL_STATE_IDLE
            }
            onState(state, number)
        }
    }

    fun start() {
        if (started) return
        started = true
        context.registerReceiver(phoneReceiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        if (Build.VERSION.SDK_INT >= 31) {
            val cb = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) = onState(state, "")
            }
            telephony.registerTelephonyCallback(context.mainExecutor, cb)
        } else {
            @Suppress("DEPRECATION")
            telephony.listen(
                object : android.telephony.PhoneStateListener() {
                    @Deprecated("Deprecated in Java")
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        onState(state, phoneNumber.orEmpty())
                    }
                },
                android.telephony.PhoneStateListener.LISTEN_CALL_STATE,
            )
        }
    }

    fun onState(state: Int, number: String) {
        val phase = when (state) {
            TelephonyManager.CALL_STATE_RINGING -> CallPhase.RINGING
            TelephonyManager.CALL_STATE_OFFHOOK -> CallPhase.OFFHOOK
            else -> CallPhase.IDLE
        }
        if (phase != CallPhase.RINGING) {
            lastPhase = phase
            lastNumber = ""
            tts.interrupt()
            return
        }
        scope.launch {
            var resolved = lookup.resolve(number)
            if (resolved.isBlank()) {
                delay(400)
                resolved = lookup.resolve(number)
            }
            if (lastPhase == CallPhase.RINGING && lastNumber.isNotBlank()) {
                if (resolved.isBlank() || resolved == lastNumber) return@launch
            }
            lastPhase = CallPhase.RINGING
            lastNumber = resolved
            val snap = settings.snapshot()
            if (!snap.callsEnabled) return@launch
            if (!gate.allow(snap)) return@launch
            val event = IncomingCallEvent(resolved, contacts.nameFor(resolved), phase)
            tts.speak(
                SpokenEvent(
                    SpokenEvent.Kind.CALL,
                    TtsFormat.incomingCall(event.spokenName),
                    looping = true,
                ),
            )
        }
    }
}
