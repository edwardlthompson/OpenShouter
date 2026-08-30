package org.openshouter.astro.model

import java.time.DayOfWeek
import java.time.Instant

enum class SolarEventType {
    Sunrise,
    Sunset,
    Dawn,
    Dusk,
    CivilDawn,
    CivilDusk,
    NauticalDawn,
    NauticalDusk,
    AstronomicalDawn,
    AstronomicalDusk,
    SolarNoon,
    SolarMidnight,
    GoldenHourMorning,
    GoldenHourEvening,
    BlueHourMorning,
    BlueHourEvening,
    MarchEquinox,
    SeptemberEquinox,
    JuneSolstice,
    DecemberSolstice
}

enum class LunarEventType {
    Moonrise,
    Moonset,
    MoonTransit,
    NewMoon,
    FullMoon,
    WaxingCrescent,
    FirstQuarter,
    WaxingGibbous,
    WaningGibbous,
    LastQuarter,
    WaningCrescent
}

sealed interface AlarmTarget {
    data class CustomClock(val hour: Int, val minute: Int) : AlarmTarget
    data class Solar(val event: SolarEventType, val offsetMinutes: Int = 0) : AlarmTarget
    data class Lunar(val event: LunarEventType, val offsetMinutes: Int = 0) : AlarmTarget
}

data class AstroAlarm(
    val id: String,
    val label: String,
    val enabled: Boolean = true,
    val target: AlarmTarget,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val toneEnabled: Boolean = true,
    val toneUri: String? = null,
    val ttsEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val lastFiredEpochMs: Long = 0L
) {
    val isOnce: Boolean get() = daysOfWeek.isEmpty()
}
