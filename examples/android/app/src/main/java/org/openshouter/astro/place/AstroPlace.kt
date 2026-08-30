package org.openshouter.astro.place

import java.time.ZoneId

data class AstroPlace(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String = ZoneId.systemDefault().id
) {
    val zone: ZoneId get() = runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())

    val isValid: Boolean
        get() = cityName.isNotBlank() && latitude in -90.0..90.0 && longitude in -180.0..180.0
}
