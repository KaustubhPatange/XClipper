package com.kpstv.xclipper.service

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class TileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, ChangeClipboardActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_MULTIPLE_TASK
        }
        startActivityAndCollapse(intent)
    }

    override fun onStartListening() {
        qsTile?.state = 2
        qsTile?.updateTile()
        super.onStartListening()
    }
}