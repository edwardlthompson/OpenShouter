package org.openshouter.astro.alarm

import org.json.JSONArray
import org.json.JSONObject
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import org.openshouter.astro.model.LunarEventType
import org.openshouter.astro.model.SolarEventType
import java.time.DayOfWeek
import java.util.UUID

object AstroAlarmJson {
    fun toJson(alarm: AstroAlarm): JSONObject {
        val obj = JSONObject()
        obj.put("id", alarm.id)
        obj.put("label", alarm.label)
        obj.put("enabled", alarm.enabled)
        obj.put("toneEnabled", alarm.toneEnabled)
        obj.put("toneUri", alarm.toneUri ?: JSONObject.NULL)
        obj.put("ttsEnabled", alarm.ttsEnabled)
        obj.put("vibrateEnabled", alarm.vibrateEnabled)
        obj.put("snoozeMinutes", alarm.snoozeMinutes)
        obj.put("mathUnlockEnabled", alarm.mathUnlockEnabled)
        obj.put("lastFiredEpochMs", alarm.lastFiredEpochMs)

        val daysArr = JSONArray()
        alarm.daysOfWeek.forEach { daysArr.put(it.name) }
        obj.put("daysOfWeek", daysArr)

        val targetObj = JSONObject()
        when (val target = alarm.target) {
            is AlarmTarget.CustomClock -> {
                targetObj.put("kind", "clock")
                targetObj.put("hour", target.hour)
                targetObj.put("minute", target.minute)
            }
            is AlarmTarget.Solar -> {
                targetObj.put("kind", "solar")
                targetObj.put("event", target.event.name)
                targetObj.put("offset", target.offsetMinutes)
            }
            is AlarmTarget.Lunar -> {
                targetObj.put("kind", "lunar")
                targetObj.put("event", target.event.name)
                targetObj.put("offset", target.offsetMinutes)
            }
        }
        obj.put("target", targetObj)
        return obj
    }

    fun fromJson(obj: JSONObject): AstroAlarm? {
        val id = obj.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val label = obj.optString("label", "Alarm")
        val enabled = obj.optBoolean("enabled", true)
        val toneEnabled = obj.optBoolean("toneEnabled", true)
        val toneUri = if (obj.isNull("toneUri")) null else obj.optString("toneUri")
        val ttsEnabled = obj.optBoolean("ttsEnabled", true)
        val vibrateEnabled = obj.optBoolean("vibrateEnabled", true)
        val snoozeMinutes = obj.optInt("snoozeMinutes", 10)
        val mathUnlock = obj.optBoolean("mathUnlockEnabled", false)
        val lastFired = obj.optLong("lastFiredEpochMs", 0L)

        val days = mutableSetOf<DayOfWeek>()
        val daysArr = obj.optJSONArray("daysOfWeek")
        if (daysArr != null) {
            for (i in 0 until daysArr.length()) {
                val dName = daysArr.optString(i)
                runCatching { DayOfWeek.valueOf(dName) }.getOrNull()?.let { days.add(it) }
            }
        }

        val targetObj = obj.optJSONObject("target") ?: return null
        val target: AlarmTarget = when (targetObj.optString("kind")) {
            "clock" -> AlarmTarget.CustomClock(targetObj.optInt("hour", 7), targetObj.optInt("minute", 0))
            "solar" -> {
                val evName = targetObj.optString("event")
                val normalizedName = when (evName) {
                    "Dawn" -> "CivilDawn"
                    "Dusk" -> "CivilDusk"
                    else -> evName
                }
                val ev = runCatching { SolarEventType.valueOf(normalizedName) }.getOrDefault(SolarEventType.Sunrise)
                AlarmTarget.Solar(ev, targetObj.optInt("offset", 0))
            }
            "lunar" -> {
                val evName = targetObj.optString("event")
                val ev = runCatching { LunarEventType.valueOf(evName) }.getOrDefault(LunarEventType.Moonrise)
                AlarmTarget.Lunar(ev, targetObj.optInt("offset", 0))
            }
            else -> return null
        }

        return AstroAlarm(
            id = id,
            label = label,
            enabled = enabled,
            target = target,
            daysOfWeek = days,
            toneEnabled = toneEnabled,
            toneUri = toneUri,
            ttsEnabled = ttsEnabled,
            vibrateEnabled = vibrateEnabled,
            snoozeMinutes = snoozeMinutes,
            mathUnlockEnabled = mathUnlock,
            lastFiredEpochMs = lastFired
        )
    }
}
