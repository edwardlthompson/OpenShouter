package org.openshouter.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class HeadsetOnlyTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        val newState = if (tile.state == Tile.STATE_ACTIVE) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.state = newState
        tile.updateTile()
    }
}
