package org.openshouter.geo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.data.PlaceDao
import org.openshouter.domain.GeoFence
import org.openshouter.domain.GeoPlace

@Singleton
class GeoMonitor @Inject constructor(
    @ApplicationContext context: Context,
    private val places: PlaceDao,
) {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    @Volatile var last: Location? = null
        private set

    suspend fun insideSilent(): Boolean {
        val loc = last ?: manager.getLastKnownLocationSafe()
        val stored = places.snapshot().map {
            GeoPlace(it.id, it.label, it.latitude, it.longitude, it.radiusMeters, it.silentInside)
        }
        return GeoFence.insideSilent(stored, loc?.latitude, loc?.longitude)
    }

    @SuppressLint("MissingPermission")
    fun start() {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                last = location
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }
        runCatching {
            manager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 60_000L, 25f, listener)
        }
    }
}

@SuppressLint("MissingPermission")
private fun LocationManager.getLastKnownLocationSafe(): Location? =
    runCatching { getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()
        ?: runCatching { getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
        ?: runCatching { getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
