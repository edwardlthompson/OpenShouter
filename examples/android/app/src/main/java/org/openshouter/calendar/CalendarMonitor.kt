package org.openshouter.calendar

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
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
        started = true
        context.registerReceiver(this, IntentFilter(Intent.ACTION_TIME_TICK))
        scope.launch { scan() }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_TIME_TICK) return
        scope.launch { scan() }
    }

    private suspend fun scan() {
        if (!hasRead()) return
        val snap = settings.snapshot()
        if (!snap.calendarShoutEnabled) return
        if (!gate.allow(snap, ShoutChannel.CALENDAR)) return
        val lookAhead = CalendarShout.lookAheadMs(snap.calendarLookaheadMinutes)
        val now = System.currentTimeMillis()
        val event = querySoon(now, lookAhead, lastSpoken) ?: return
        val title = CalendarShout.phrase(event.title)
        lastSpoken = event.eventId to event.begin
        val spoken = context.getString(R.string.calendar_soon, title)
        ShoutHistoryStore.insertOnce(history, SpokenEvent.Kind.CALENDAR, spoken)
        tts.speak(ChannelStates.spoken(snap, ShoutChannel.CALENDAR, SpokenEvent.Kind.CALENDAR, spoken))
    }

    private fun hasRead(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    private fun querySoon(
        now: Long,
        lookAheadMs: Long,
        spoken: Pair<Long, Long>?,
    ): CalendarShout.Event? {
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString())
            .appendPath((now + lookAheadMs).toString())
            .build()
        val full = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.VISIBLE,
            CalendarContract.Instances.SELF_ATTENDEE_STATUS,
        )
        val events = load(uri, full, now) ?: load(uri, full.copyOf(3), now) ?: return null
        return CalendarShout.pickNext(events, now, spoken, lookAheadMs)
    }

    private fun load(uri: Uri, cols: Array<String>, now: Long): List<CalendarShout.Event>? = runCatching {
        val declined = CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED
        context.contentResolver.query(
            uri,
            cols,
            "${CalendarContract.Instances.BEGIN}>=?",
            arrayOf(now.toString()),
            "${CalendarContract.Instances.BEGIN} ASC",
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CalendarShout.Event(
                            eventId = cursor.getLong(0),
                            begin = cursor.getLong(1),
                            title = cursor.getString(2).orEmpty(),
                            allDay = cols.size > 3 && cursor.getInt(3) == 1,
                            visible = cols.size <= 4 || cursor.isNull(4) || cursor.getInt(4) != 0,
                            declined = cols.size > 5 && cursor.getInt(5) == declined,
                        ),
                    )
                }
            }
        }
    }.getOrNull()
}
