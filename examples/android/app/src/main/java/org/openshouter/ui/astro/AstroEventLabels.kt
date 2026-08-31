package org.openshouter.ui.astro

import org.openshouter.astro.model.LunarEventType
import org.openshouter.astro.model.SolarEventType

object AstroEventLabels {

    fun solarLabel(event: SolarEventType): String = when (event) {
        SolarEventType.Sunrise -> "Sunrise"
        SolarEventType.Sunset -> "Sunset"
        SolarEventType.CivilDawn -> "Civil Dawn"
        SolarEventType.CivilDusk -> "Civil Dusk"
        SolarEventType.NauticalDawn -> "Nautical Dawn"
        SolarEventType.NauticalDusk -> "Nautical Dusk"
        SolarEventType.AstronomicalDawn -> "Astronomical Dawn"
        SolarEventType.AstronomicalDusk -> "Astronomical Dusk"
        SolarEventType.SolarNoon -> "Solar Noon"
        SolarEventType.SolarMidnight -> "Solar Midnight"
        SolarEventType.GoldenHourMorning -> "Golden Hour (Morning)"
        SolarEventType.GoldenHourEvening -> "Golden Hour (Evening)"
        SolarEventType.BlueHourMorning -> "Blue Hour (Morning)"
        SolarEventType.BlueHourEvening -> "Blue Hour (Evening)"
        SolarEventType.MarchEquinox -> "March Equinox"
        SolarEventType.SeptemberEquinox -> "September Equinox"
        SolarEventType.JuneSolstice -> "June Solstice"
        SolarEventType.DecemberSolstice -> "December Solstice"
    }

    fun solarDescription(event: SolarEventType): String = when (event) {
        SolarEventType.Sunrise -> "Sun rises above horizon"
        SolarEventType.Sunset -> "Sun sets below horizon"
        SolarEventType.CivilDawn -> "Morning twilight (sun 6° below horizon)"
        SolarEventType.CivilDusk -> "Evening twilight (sun 6° below horizon)"
        SolarEventType.NauticalDawn -> "Nautical twilight (sun 12° below horizon)"
        SolarEventType.NauticalDusk -> "Nautical dusk (sun 12° below horizon)"
        SolarEventType.AstronomicalDawn -> "Astronomical twilight (sun 18° below horizon)"
        SolarEventType.AstronomicalDusk -> "Astronomical dusk (night begins)"
        SolarEventType.SolarNoon -> "Sun reaches highest point in sky"
        SolarEventType.SolarMidnight -> "Sun at lowest point (nadir)"
        SolarEventType.GoldenHourMorning -> "Warm golden light after sunrise"
        SolarEventType.GoldenHourEvening -> "Warm golden light before sunset"
        SolarEventType.BlueHourMorning -> "Deep blue sky before dawn"
        SolarEventType.BlueHourEvening -> "Deep blue sky after dusk"
        SolarEventType.MarchEquinox -> "Spring equinox (approx. March 20)"
        SolarEventType.SeptemberEquinox -> "Autumn equinox (approx. Sept 22)"
        SolarEventType.JuneSolstice -> "Summer solstice (approx. June 21)"
        SolarEventType.DecemberSolstice -> "Winter solstice (approx. Dec 21)"
    }

    fun lunarLabel(event: LunarEventType): String = when (event) {
        LunarEventType.Moonrise -> "Moonrise"
        LunarEventType.Moonset -> "Moonset"
        LunarEventType.MoonTransit -> "Moon Transit (Culmination)"
        LunarEventType.NewMoon -> "New Moon"
        LunarEventType.WaxingCrescent -> "Waxing Crescent"
        LunarEventType.FirstQuarter -> "First Quarter"
        LunarEventType.WaxingGibbous -> "Waxing Gibbous"
        LunarEventType.FullMoon -> "Full Moon"
        LunarEventType.WaningGibbous -> "Waning Gibbous"
        LunarEventType.LastQuarter -> "Last Quarter"
        LunarEventType.WaningCrescent -> "Waning Crescent"
    }

    fun lunarDescription(event: LunarEventType): String = when (event) {
        LunarEventType.Moonrise -> "Moon rises above eastern horizon"
        LunarEventType.Moonset -> "Moon drops below western horizon"
        LunarEventType.MoonTransit -> "Moon reaches highest meridian point"
        LunarEventType.NewMoon -> "Moon illuminated 0% (dark disk)"
        LunarEventType.WaxingCrescent -> "Silver crescent waxing after new moon"
        LunarEventType.FirstQuarter -> "Half moon illuminated 50% (growing)"
        LunarEventType.WaxingGibbous -> "Waxing moon growing toward full"
        LunarEventType.FullMoon -> "Moon illuminated 100% (full disk)"
        LunarEventType.WaningGibbous -> "Waning moon shrinking from full"
        LunarEventType.LastQuarter -> "Half moon illuminated 50% (shrinking)"
        LunarEventType.WaningCrescent -> "Silver crescent before new moon"
    }

    fun offsetSummary(offsetMinutes: Int, eventName: String): String = when {
        offsetMinutes == 0 -> "At exact time of $eventName"
        offsetMinutes < 0 -> "${-offsetMinutes} min before $eventName"
        else -> "$offsetMinutes min after $eventName"
    }
}
