package com.kpstv.xclipper.extensions.utils

import android.content.ComponentName
import android.content.Context
import com.kpstv.core.BuildConfig
import com.kpstv.xclipper.service.ClipboardAccessibilityService

object ClipboardUtils {
    fun openServiceAccessibilitySetting(context: Context) = with(context) {
        SystemUtils.openAccessibilitySettings(
            context = this,
            componentName = ComponentName(
                BuildConfig.APPLICATION_ID,
                ClipboardAccessibilityService::class.java.name
            )
        )
    }
}