package com.kpstv.xclipper.ui.helpers

import android.content.Context
import android.view.ViewGroup
import com.kpstv.xclipper.extensions.QuickTip
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.service.ClipboardAccessibilityService

object AccessibilityQuickTipHelper {

    fun addQuickTip(container: ViewGroup, doOnAction: SimpleFunction) {
        val context = container.context

        val showQuickTip = canShowQuickTip(context)
        if (showQuickTip) {
            val tipView = QuickTip(container).run {
                setTitleText(R.string.qt_service_title)
                setSubText(R.string.qt_service_summary)
                setIcon(R.drawable.ic_cross)
                applyColor(context.colorFrom(R.color.light_red))
                hideButtonPanel()
                setOnClick(doOnAction)
                updatePadding(top = 7.dp())
                create()
            }
            container.addView(tipView)
        }
    }

    private fun canShowQuickTip(context: Context): Boolean {
        return !ClipboardAccessibilityService.isRunning(context)
    }
}