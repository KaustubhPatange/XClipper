package com.kpstv.xclipper.extensions

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils.calculateLuminance

object ColorExUtils {
    fun isDarkColor(@ColorInt color: Int) : Boolean {
        return calculateLuminance(color) < 0.5
    }
    fun isLightColor(@ColorInt color: Int) : Boolean {
        return !isDarkColor(color)
    }
}