package org.openshouter.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class TimeShoutMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val announcer: TimeShoutAnnouncer,
) : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile private var started = false

    fun start() {
        if (started) return
        started = true
        context.registerReceiver(this, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_TIME_TICK) return
        scope.launch { announcer.announce(requireAligned = true) }
    }
}
