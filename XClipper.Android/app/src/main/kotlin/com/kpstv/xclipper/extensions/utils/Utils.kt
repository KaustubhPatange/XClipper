package com.kpstv.xclipper.extensions.utils

import android.content.ComponentName
import android.content.Context
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.service.ClipboardAccessibilityService

class Utils {
    companion object {
        fun openClipboardServiceAccessibility(context: Context) = with(context) {
            SystemUtils.openAccessibilitySettings(
                context = this,
                componentName = ComponentName(
                    BuildConfig.APPLICATION_ID,
                    ClipboardAccessibilityService::class.java.name
                )
            )
        }
    }
}