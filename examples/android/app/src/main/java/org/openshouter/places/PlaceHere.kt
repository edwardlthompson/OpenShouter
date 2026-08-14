package org.openshouter.places

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import org.openshouter.data.PlaceEntity
import org.openshouter.service.OpenShouterEntryPoint

object PlaceHere {
    @SuppressLint("MissingPermission")
    suspend fun save(context: Context, ep: OpenShouterEntryPoint, label: String) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = runCatching { lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()
            ?: runCatching { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
            ?: runCatching { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
            ?: return
        ep.places().insert(
            PlaceEntity(
                label = label,
                latitude = loc.latitude,
                longitude = loc.longitude,
                radiusMeters = 150f,
                silentInside = true,
            ),
        )
    }
}
