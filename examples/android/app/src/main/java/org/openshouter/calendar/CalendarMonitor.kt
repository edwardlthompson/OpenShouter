package org.openshouter.calendar

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.foss.goldenpath.R
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.openshouter.data.HistoryDao
import org.openshouter.data.SettingsRepository
import org.openshouter.data.ShoutHistoryStore
import org.openshouter.domain.ChannelStates
import org.openshouter.domain.ShoutChannel
import org.openshouter.domain.SpokenEvent
import org.openshouter.service.SpeakGate
import org.openshouter.tts.TtsController

@Singleton
class CalendarMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val tts: TtsController,
    private val gate: SpeakGate,
    private val history: HistoryDao,
) : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile private var started = false
    @Volatile private var lastSpoken: Pair<Long, Long>? = null

    fun start() {
        if (started) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        started = true
        context.registerReceiver(this, IntentFilter(Intent.ACTION_TIME_TICK))
        scope.launch { scan() }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_TIME_TICK) return
        scope.launch { scan() }
    }

    private suspend fun scan() {
        val snap = settings.snapshot()
        if (!snap.calendarShoutEnabled) return
        if (!gate.allow(snap, ShoutChannel.CALENDAR)) return
        val lookAhead = CalendarShout.lookAheadMs(snap.calendarLookaheadMinutes)
        val event = querySoon(lookAhead) ?: return
        val now = System.currentTimeMillis()
        if (!CalendarShout.shouldSpeak(event.first, event.second, now, lastSpoken, lookAhead)) return
        val title = CalendarShout.phrase(event.third)
        if (title.isEmpty()) return
        lastSpoken = event.first to event.second
        val spoken = context.getString(R.string.calendar_soon, title)
        ShoutHistoryStore.insertOnce(history, SpokenEvent.Kind.CALENDAR, spoken)
        tts.speak(ChannelStates.spoken(snap, ShoutChannel.CALENDAR, SpokenEvent.Kind.CALENDAR, spoken))
    }

    private fun querySoon(lookAheadMs: Long): Triple<Long, Long, String>? = runCatching {
        val now = System.currentTimeMillis()
        val end = now + lookAheadMs
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString())
            .appendPath(end.toString())
            .build()
        val cols = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.TITLE,
        )
        context.contentResolver.query(
            uri,
            cols,
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC",
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            Triple(cursor.getLong(0), cursor.getLong(1), cursor.getString(2).orEmpty())
        }
    }.getOrNull()
}
