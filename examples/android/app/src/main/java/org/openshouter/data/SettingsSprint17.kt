package org.openshouter.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import org.openshouter.calendar.CalendarShout
import org.openshouter.domain.AppSettings
import org.openshouter.domain.NotificationRank

internal object SettingsSprint17 {
    val COLLAPSE_REPEATS = booleanPreferencesKey("collapse_repeats")
    val MIN_IMPORTANCE = stringPreferencesKey("min_importance")
    val DND_PRIORITY = booleanPreferencesKey("dnd_priority")
    val CAL_LOOKAHEAD = intPreferencesKey("cal_lookahead")
    val SHOW_SPOKEN = booleanPreferencesKey("history_show_spoken")
    val IGNORE_BUBBLES = booleanPreferencesKey("ignore_bubbles")
    val IGNORE_WORK_PROFILE = booleanPreferencesKey("ignore_work_profile")
    val CONTACT_COOLDOWN = intPreferencesKey("contact_cooldown_sec")

    fun apply(base: AppSettings, prefs: Preferences): AppSettings = base.copy(
        notificationPolicy = base.notificationPolicy.copy(
            collapseRepeats = prefs[COLLAPSE_REPEATS] ?: true,
            minImportance = NotificationRank.parseImportance(prefs[MIN_IMPORTANCE]),
            dndPriorityOnly = prefs[DND_PRIORITY] ?: true,
            ignoreBubbles = prefs[IGNORE_BUBBLES] ?: true,
            ignoreWorkProfile = prefs[IGNORE_WORK_PROFILE] ?: false,
            contactCooldownSeconds = prefs[CONTACT_COOLDOWN] ?: 0,
        ),
        calendarLookaheadMinutes = CalendarShout.clampMinutes(
            prefs[CAL_LOOKAHEAD] ?: CalendarShout.DEFAULT_MINUTES,
        ),
        showSpokenText = prefs[SHOW_SPOKEN] ?: false,
    )
}
