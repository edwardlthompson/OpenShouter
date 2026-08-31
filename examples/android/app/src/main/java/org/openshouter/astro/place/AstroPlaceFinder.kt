package org.openshouter.astro.place

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import java.time.ZoneId
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object AstroPlaceFinder {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun resolveLocation(context: Context): AstroPlace? {
        if (!hasLocationPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val lastLoc = runCatching {
            val gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val passive = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            listOfNotNull(gps, net, passive).maxByOrNull { it.time }
        }.getOrNull()

        val loc = if (lastLoc != null && System.currentTimeMillis() - lastLoc.time < 30 * 60 * 1000L) {
            lastLoc
        } else {
            fetchFreshLocation(context, lm) ?: lastLoc
        } ?: return null

        return placeFromCoordinates(context, loc.latitude, loc.longitude)
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFreshLocation(context: Context, lm: LocationManager): Location? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val provider = if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationManager.GPS_PROVIDER
            } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationManager.NETWORK_PROVIDER
            } else {
                LocationManager.PASSIVE_PROVIDER
            }
            return suspendCancellableCoroutine { continuation ->
                val signal = CancellationSignal()
                continuation.invokeOnCancellation { signal.cancel() }
                try {
                    lm.getCurrentLocation(
                        provider,
                        signal,
                        ContextCompat.getMainExecutor(context)
                    ) { location ->
                        if (continuation.isActive) continuation.resume(location)
                    }
                } catch (_: SecurityException) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun placeFromCoordinates(context: Context, lat: Double, lon: Double): AstroPlace {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = runCatching {
            geocoder.getFromLocation(lat, lon, 1)
        }.getOrNull()

        val addr = addresses?.firstOrNull()
        val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
        val label = if (city != null) {
            listOfNotNull(city, addr?.adminArea, addr?.countryName).distinct().joinToString(", ")
        } else {
            String.format(Locale.US, "%.4f°, %.4f°", lat, lon)
        }

        return AstroPlace(
            cityName = label,
            latitude = lat,
            longitude = lon,
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
