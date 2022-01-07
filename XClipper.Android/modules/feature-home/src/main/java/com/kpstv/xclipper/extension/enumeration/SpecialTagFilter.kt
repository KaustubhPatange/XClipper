package com.kpstv.xclipper.extension.enumeration

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kpstv.xclipper.feature_home.R

/**
 * Each tag has some effects on the behavior of how lists of clips are shown.
 */
enum class SpecialTagFilter(@StringRes val stringRes: Int, @DrawableRes val drawableRes: Int) {
    Invert(R.string.invert, R.drawable.ic_filter_reverse); // inverts the list with the applied filter

    fun isInvert() : Boolean = this == Invert
}