package org.openshouter.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPlace(
    val id: Long = 0,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val silentInside: Boolean = true,
)

object GeoFence {
    private const val EARTH_M = 6_371_000.0

    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_M * atan2(sqrt(a), sqrt(1 - a))
    }

    fun isInside(place: GeoPlace, latitude: Double, longitude: Double): Boolean =
        distanceMeters(place.latitude, place.longitude, latitude, longitude) <= place.radiusMeters

    fun insideSilent(places: List<GeoPlace>, latitude: Double?, longitude: Double?): Boolean {
        if (latitude == null || longitude == null) return false
        return places.any { it.silentInside && isInside(it, latitude, longitude) }
    }
}
