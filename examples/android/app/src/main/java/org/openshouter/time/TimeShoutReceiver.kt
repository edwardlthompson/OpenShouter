package org.openshouter.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openshouter.data.SettingsRepository
import org.openshouter.service.OpenShouterRuntime

@AndroidEntryPoint
class TimeShoutReceiver : BroadcastReceiver() {
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var scheduler: TimeShoutScheduler
    @Inject lateinit var announcer: TimeShoutAnnouncer

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                OpenShouterRuntime.ensureStarted(app)
                scheduler.sync(settings.snapshot())
                announcer.announce(requireAligned = false)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION = "org.openshouter.action.TIME_SHOUT"
    }
}
