package com.kpstv.xclipper.ui.dialogs

import android.content.Context
import android.os.Build
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.core_clipboard.R
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.utils.ClipboardUtils
import com.kpstv.xclipper.service.ClipboardAccessibilityService

object ClipboardServiceDialogs {

    /* Accessibility Dialogs */

    fun showAccessibilityDialog(
        context: Context,
        onPositiveButtonClick: SimpleFunction = {},
        onNegativeButtonClick: SimpleFunction = {}
    ): Unit = with(context) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.accessibility_service))
            .setMessage(context.getString(R.string.accessibility_capture))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                ClipboardUtils.openServiceAccessibilitySetting(this)
                onPositiveButtonClick.invoke()
            }
            .setCancelable(false)
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> onNegativeButtonClick.invoke() }
            .show()
    }

    fun showDisableAccessibilityDialog(
        context: Context,
        onPositiveButtonClick: SimpleFunction = {},
        onNegativeButtonClick: SimpleFunction = {}
    ): Unit = with(context) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.accessibility_service_disable))
            .setMessage(getString(R.string.accessibility_disable_text))
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ClipboardAccessibilityService.disableService(context)
                } else ClipboardUtils.openServiceAccessibilitySetting(context)
                onPositiveButtonClick.invoke()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> onNegativeButtonClick.invoke() }
            .show()
    }
}