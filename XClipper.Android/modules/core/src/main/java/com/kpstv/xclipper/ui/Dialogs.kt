package com.kpstv.xclipper.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.core.R
import com.kpstv.xclipper.extensions.utils.SystemUtils

object CoreDialogs {
    /* Suggestion dialog */

    @RequiresApi(Build.VERSION_CODES.M)
    fun showSystemOverlayDialog(context: Context): AlertDialog = with(context) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.suggestion_title))
            .setMessage(getString(R.string.suggestion_capture))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                SystemUtils.openSystemOverlaySettings(this)
            }
            .setCancelable(false)
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }
}