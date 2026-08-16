package org.openshouter.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openshouter.data.SettingsRepository
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.domain.TimeShout
import org.openshouter.domain.TtsFormat
import org.openshouter.service.OpenShouterRuntime
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

@AndroidEntryPoint
class TimeShoutReceiver : BroadcastReceiver() {
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var tts: TtsController
    @Inject lateinit var gate: SpeakGate
    @Inject lateinit var scheduler: TimeShoutScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                OpenShouterRuntime.ensureStarted(app)
                val snap = settings.snapshot()
                scheduler.sync(snap)
                if (!snap.announcerEnabled || !snap.timeShoutEnabled) return@launch
                if (!gate.allow(snap, ShoutChannel.TIME)) return@launch
                val clock = TimeShout.formatClock(
                    ZonedDateTime.now(),
                    snap.timeHourStyle,
                    DateFormat.is24HourFormat(app),
                )
                val phrase = TtsFormat.time(snap.timeFormat, clock)
                if (phrase.isNotBlank()) {
                    tts.speak(ChannelStates.spoken(snap, ShoutChannel.TIME, SpokenEvent.Kind.TIME, phrase))
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION = "org.openshouter.action.TIME_SHOUT"
    }
}
