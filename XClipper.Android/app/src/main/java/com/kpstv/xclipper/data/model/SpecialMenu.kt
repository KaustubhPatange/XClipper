package com.kpstv.xclipper.data.model

import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.SimpleFunction

data class SpecialMenu (
    @DrawableRes
    val image: Int,
    val title: String,
    @ColorRes
    val imageTint: Int = -1,
    @ColorRes
    val textColor: Int = -1,
    val onClick: SimpleFunction
)