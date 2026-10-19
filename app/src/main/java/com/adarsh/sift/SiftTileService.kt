package com.adarsh.sift

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.os.Build

class SiftTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile
        if (tile != null) {
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, CaptureActivity::class.java).apply {
            putExtra("source", "TILE")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            val pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
