package org.openshouter.call

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openshouter.contacts.ContactsLookup
import org.openshouter.data.HistoryDao
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.CallPhase
import org.openshouter.domain.IncomingCallEvent
import org.openshouter.service.SpeakGate
import org.openshouter.telephony.SimLine
import org.openshouter.tts.TtsController

@Singleton
class CallMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val contacts: ContactsLookup,
    private val lookup: CallLogLookup,
    private val tts: TtsController,
    private val gate: SpeakGate,
    private val history: HistoryDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    @Volatile private var started = false
    @Volatile private var lastPhase = CallPhase.IDLE
    @Volatile private var lastNumber = ""
    @Volatile private var lastSim = ""
    @Volatile private var historyLogged = false
    @Volatile private var offhookStartMs = 0L
    private val phoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
            val extra = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER).orEmpty()
            val sim = SimLine.resolve(context, intent)
            val state = when (extra) {
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                else -> TelephonyManager.CALL_STATE_IDLE
            }
            onState(state, number, sim)
        }
    }

    fun start() {
        if (started) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        runCatching {
            ContextCompat.registerReceiver(
                context,
                phoneReceiver,
                IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED),
                ContextCompat.RECEIVER_EXPORTED,
            )
        }
        runCatching {
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
            started = true
        }
    }

    fun onState(state: Int, number: String, sim: String = "") {
        val phase = when (state) {
            TelephonyManager.CALL_STATE_RINGING -> CallPhase.RINGING
            TelephonyManager.CALL_STATE_OFFHOOK -> CallPhase.OFFHOOK
            else -> CallPhase.IDLE
        }
        if (phase == CallPhase.OFFHOOK) {
            if (lastPhase != CallPhase.OFFHOOK) offhookStartMs = System.currentTimeMillis()
            lastPhase = CallPhase.OFFHOOK
            tts.interrupt()
            return
        }
        if (phase != CallPhase.RINGING) {
            val wasOffhook = lastPhase == CallPhase.OFFHOOK
            val wasRinging = lastPhase == CallPhase.RINGING
            val ringingNumber = lastNumber
            val durSec = if (wasOffhook && offhookStartMs > 0) (System.currentTimeMillis() - offhookStartMs) / 1000L else 0L
            lastPhase = phase
            lastNumber = ""
            offhookStartMs = 0L
            historyLogged = false
            tts.interrupt()
            if (wasRinging && phase == CallPhase.IDLE && ringingNumber.isNotBlank()) {
                scope.launch { CallMonitorState.handleMissed(settings.snapshot(), gate, tts, history, contacts, ringingNumber) }
            } else if (wasOffhook && durSec > 0) {
                scope.launch { CallMonitorState.handleHangup(settings.snapshot(), gate, tts, history, durSec) }
            }
            return
        }
        val isCallWaiting = lastPhase == CallPhase.OFFHOOK
        scope.launch {
            var resolved = lookup.resolve(number)
            if (resolved.isBlank()) {
                delay(400)
                resolved = lookup.resolve(number)
            }
            if (lastPhase == CallPhase.RINGING && lastNumber.isNotBlank()) {
                if (resolved.isBlank() || resolved == lastNumber) return@launch
            }
            if (!isCallWaiting) lastPhase = CallPhase.RINGING
            lastNumber = resolved
            lastSim = sim.ifBlank { lastSim }
            val displayName = contacts.nameFor(resolved).orEmpty()
            CallMonitorState.handleRinging(
                settings, gate, tts, history, resolved, displayName, lastSim, isCallWaiting,
            ) { historyLogged = true }
        }
    }
}
