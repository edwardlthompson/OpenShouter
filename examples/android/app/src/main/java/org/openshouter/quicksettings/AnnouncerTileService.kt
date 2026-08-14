package org.openshouter.quicksettings

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openshouter.data.SettingsRepository
import org.openshouter.service.OpenShouterEntryPoint
import org.openshouter.service.OpenShouterRuntime

class AnnouncerTileService : TileService() {
    override fun onStartListening() {
        qsTile?.let { tile ->
            val enabled = runBlocking { repo().settings.first().announcerEnabled }
            tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }

    override fun onClick() {
        runBlocking {
            val current = repo().snapshot().announcerEnabled
            repo().setEnabled(!current)
        }
        OpenShouterRuntime.ensureStarted(applicationContext)
        onStartListening()
    }

    private fun repo(): SettingsRepository =
        EntryPointAccessors.fromApplication(applicationContext, OpenShouterEntryPoint::class.java)
            .settings()
}
