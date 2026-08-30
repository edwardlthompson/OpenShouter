package org.openshouter.astro.alarm

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import org.openshouter.astro.model.AlarmTarget
import org.openshouter.astro.model.AstroAlarm
import org.openshouter.astro.model.LunarEventType
import org.openshouter.astro.model.SolarEventType
import java.time.DayOfWeek
import java.util.UUID

class AstroAlarmStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("os_astro_alarms_prefs", Context.MODE_PRIVATE)

    private val _alarms = MutableStateFlow(loadAlarms())
    val alarms: StateFlow<List<AstroAlarm>> = _alarms.asStateFlow()

    fun getAll(): List<AstroAlarm> = _alarms.value

    fun getById(id: String): AstroAlarm? = _alarms.value.firstOrNull { it.id == id }

    fun save(alarm: AstroAlarm) {
        val current = _alarms.value.toMutableList()
        val idx = current.indexOfFirst { it.id == alarm.id }
        if (idx >= 0) {
            current[idx] = alarm
        } else {
            current.add(alarm)
        }
        persist(current)
    }

    fun delete(id: String) {
        val updated = _alarms.value.filter { it.id != id }
        persist(updated)
    }

    fun toggle(id: String, enabled: Boolean) {
        val updated = _alarms.value.map {
            if (it.id == id) it.copy(enabled = enabled) else it
        }
        persist(updated)
    }

    private fun persist(list: List<AstroAlarm>) {
        val array = JSONArray()
        list.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_ALARMS_JSON, array.toString()).apply()
        _alarms.value = list
    }

    private fun loadAlarms(): List<AstroAlarm> {
        val raw = prefs.getString(KEY_ALARMS_JSON, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                fromJson(obj)
            }
        }.getOrDefault(emptyList())
    }

    private fun toJson(alarm: AstroAlarm): JSONObject {
        val obj = JSONObject()
        obj.put("id", alarm.id)
        obj.put("label", alarm.label)
        obj.put("enabled", alarm.enabled)
        obj.put("toneEnabled", alarm.toneEnabled)
        obj.put("toneUri", alarm.toneUri ?: JSONObject.NULL)
        obj.put("ttsEnabled", alarm.ttsEnabled)
        obj.put("vibrateEnabled", alarm.vibrateEnabled)
        obj.put("snoozeMinutes", alarm.snoozeMinutes)
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

    private fun fromJson(obj: JSONObject): AstroAlarm? {
        val id = obj.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val label = obj.optString("label", "Alarm")
        val enabled = obj.optBoolean("enabled", true)
        val toneEnabled = obj.optBoolean("toneEnabled", true)
        val toneUri = if (obj.isNull("toneUri")) null else obj.optString("toneUri")
        val ttsEnabled = obj.optBoolean("ttsEnabled", true)
        val vibrateEnabled = obj.optBoolean("vibrateEnabled", true)
        val snoozeMinutes = obj.optInt("snoozeMinutes", 10)
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
                val ev = runCatching { SolarEventType.valueOf(evName) }.getOrDefault(SolarEventType.Sunrise)
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
            lastFiredEpochMs = lastFired
        )
    }

    companion object {
        private const val KEY_ALARMS_JSON = "os_astro_alarms_list_json"
    }
}
