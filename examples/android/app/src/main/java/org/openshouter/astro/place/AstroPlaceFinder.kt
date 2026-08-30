package org.openshouter.astro.place

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import java.time.ZoneId
import java.util.Locale

object AstroPlaceFinder {

    @Suppress("DEPRECATION")
    fun resolveFromLocation(context: Context): AstroPlace? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val loc = runCatching {
            lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }.getOrNull() ?: return null

        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = runCatching {
            geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
        }.getOrNull()

        val addr = addresses?.firstOrNull()
        val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: "Current Location"
        return AstroPlace(
            cityName = city,
            latitude = loc.latitude,
            longitude = loc.longitude,
            zoneId = ZoneId.systemDefault().id
        )
    }

    @Suppress("DEPRECATION")
    fun searchCities(context: Context, query: String, maxResults: Int = 5): List<AstroPlace> {
        if (query.trim().length < 2) return emptyList()
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = runCatching {
            geocoder.getFromLocationName(query.trim(), maxResults)
        }.getOrNull() ?: return emptyList()

        return addresses.mapNotNull { addr ->
            val city = addr.locality ?: addr.featureName ?: addr.adminArea ?: return@mapNotNull null
            val label = listOfNotNull(city, addr.adminArea, addr.countryName)
                .distinct()
                .joinToString(", ")
            AstroPlace(
                cityName = label,
                latitude = addr.latitude,
                longitude = addr.longitude,
                zoneId = ZoneId.systemDefault().id
            )
        }
    }
}
