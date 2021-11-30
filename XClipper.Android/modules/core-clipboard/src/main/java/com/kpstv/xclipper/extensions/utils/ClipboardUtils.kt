package com.kpstv.xclipper.extensions.utils

import android.content.ComponentName
import android.content.Context

object ClipboardUtils {
    fun openServiceAccessibilitySetting(context: Context) = with(context) {
        SystemUtils.openAccessibilitySettings(
            context = this,
            componentName = ComponentName(
//                BuildConfig.APPLICATION_ID,
                context.packageName, // TODO: Change to BuildConfig
//                ClipboardAccessibilityService::class.java.name // TODO: Change to accessibility service
                Class.forName("com.kpstv.xclipper.service.ClipboardAccessibilityService").name,
            )
        )
    }
}