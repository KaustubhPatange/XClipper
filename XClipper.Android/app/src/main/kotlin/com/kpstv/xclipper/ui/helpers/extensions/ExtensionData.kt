package com.kpstv.xclipper.ui.helpers.extensions

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

interface ExtensionData {
    val title: String
    val description: String

    @get:DrawableRes
    val icon: Int

    @get:ColorInt
    val dominantColor: Int

    val sku: String
}