package com.kpstv.xclipper.data.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

interface ExtensionData {
    val title: String
    val fullDescription: String
    val smallDescription: String

    @get:DrawableRes
    val icon: Int

    @get:ColorInt
    val dominantColor: Int

    val sku: String
}