package org.openshouter.service

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.openshouter.data.AppSpeakStore
import org.openshouter.data.HistoryDao
import org.openshouter.data.RegexDao
import org.openshouter.data.SettingsRepository
import org.openshouter.tts.TtsController

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OpenShouterEntryPoint {
    fun settings(): SettingsRepository
    fun tts(): TtsController
    fun history(): HistoryDao
    fun regex(): RegexDao
    fun places(): org.openshouter.data.PlaceDao
    fun appSpeak(): AppSpeakStore
    fun gate(): SpeakGate
    fun audio(): org.openshouter.audio.AudioRouteMonitor
    fun calls(): org.openshouter.call.CallMonitor
    fun timeShout(): org.openshouter.time.TimeShoutScheduler
    fun reminders(): org.openshouter.data.ReminderDao
    fun soundLeaks(): org.openshouter.data.SoundLeakDao
    fun audioSessions(): org.openshouter.silence.AudioSessionMonitor
    fun sprint13(): org.openshouter.data.Sprint13Settings
    fun sprint15(): org.openshouter.data.Sprint15Settings
    fun alarms(): org.openshouter.alarm.AlarmScheduler
}
