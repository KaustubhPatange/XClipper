package com.kpstv.xclipper.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.drawableFrom(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}