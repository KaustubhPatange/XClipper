package com.kpstv.xclipper.data.model

import androidx.annotation.DrawableRes

data class SpecialMenu (
    @DrawableRes
    val image: Int,
    val title: String,
    val onClick: () -> Unit
)