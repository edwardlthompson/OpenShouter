package org.openshouter.astro.place

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AstroPlaceStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("os_astro_place_prefs", Context.MODE_PRIVATE)

    private val _place = MutableStateFlow(load())
    val place: StateFlow<AstroPlace?> = _place.asStateFlow()

    fun get(): AstroPlace? = _place.value

    fun set(place: AstroPlace) {
        prefs.edit()
            .putString(KEY_CITY, place.cityName)
            .putFloat(KEY_LAT, place.latitude.toFloat())
            .putFloat(KEY_LON, place.longitude.toFloat())
            .putString(KEY_TZ, place.zoneId)
            .apply()
        _place.value = place
    }

    fun clear() {
        prefs.edit().clear().apply()
        _place.value = null
    }

    private fun load(): AstroPlace? {
        val city = prefs.getString(KEY_CITY, null) ?: return null
        val lat = prefs.getFloat(KEY_LAT, Float.NaN)
        val lon = prefs.getFloat(KEY_LON, Float.NaN)
        val tz = prefs.getString(KEY_TZ, null) ?: return null
        if (lat.isNaN() || lon.isNaN()) return null
        return AstroPlace(city, lat.toDouble(), lon.toDouble(), tz)
    }

    companion object {
        private const val KEY_CITY = "os_astro_city"
        private const val KEY_LAT = "os_astro_lat"
        private const val KEY_LON = "os_astro_lon"
        private const val KEY_TZ = "os_astro_tz"
    }
}
