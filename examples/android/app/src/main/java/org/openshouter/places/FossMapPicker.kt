package org.openshouter.places

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.tan

data class MapFence(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val label: String,
    val silentInside: Boolean = true,
)

object FossMapPicker {
    const val MIN_RADIUS_METERS = 50f
    const val DEFAULT_RADIUS_METERS = 150f
    const val MAX_RADIUS_METERS = 2000f

    fun clampRadius(meters: Float): Float =
        meters.coerceIn(MIN_RADIUS_METERS, MAX_RADIUS_METERS)

    fun osmTileUrl(x: Int, y: Int, zoom: Int): String =
        "https://tile.openstreetmap.org/$zoom/$x/$y.png"

    fun lonToTileX(lon: Double, zoom: Int): Int {
        val n = 1 shl zoom
        return ((lon + 180.0) / 360.0 * n).toInt().coerceIn(0, n - 1)
    }

    fun latToTileY(lat: Double, zoom: Int): Int {
        val latRad = Math.toRadians(lat)
        val n = 1 shl zoom
        val y = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / Math.PI) / 2.0 * n
        return y.toInt().coerceIn(0, n - 1)
    }
}
