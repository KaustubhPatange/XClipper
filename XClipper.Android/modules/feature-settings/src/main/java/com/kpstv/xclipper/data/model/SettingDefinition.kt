package com.kpstv.xclipper.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kpstv.xclipper.extensions.FragClazz

data class SettingDefinition(
    val clazz: FragClazz,
    @StringRes val titleRes: Int,
    @DrawableRes val drawableRes: Int
)