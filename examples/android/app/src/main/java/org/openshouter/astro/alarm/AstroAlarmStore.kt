package org.openshouter.astro.alarm

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.openshouter.astro.model.AstroAlarm

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
        list.forEach { array.put(AstroAlarmJson.toJson(it)) }
        prefs.edit().putString(KEY_ALARMS_JSON, array.toString()).apply()
        _alarms.value = list
    }

    private fun loadAlarms(): List<AstroAlarm> {
        val raw = prefs.getString(KEY_ALARMS_JSON, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                AstroAlarmJson.fromJson(obj)
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val KEY_ALARMS_JSON = "os_astro_alarms_list_json"
    }
}
