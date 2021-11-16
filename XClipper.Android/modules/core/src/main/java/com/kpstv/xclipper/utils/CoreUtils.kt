package com.kpstv.xclipper.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object CoreUtils {
    fun isSystemOverlayEnabled(context: Context): Boolean = with(context) {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }
}