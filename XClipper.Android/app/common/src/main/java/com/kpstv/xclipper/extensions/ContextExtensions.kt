package com.kpstv.xclipper.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Context.layoutInflater(): LayoutInflater = LayoutInflater.from(this)

fun Context.drawableFrom(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

fun Fragment.drawableFrom(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(requireContext(), id)
}

fun Context.hasThemeColorAttribute(@AttrRes id: Int): Boolean {
    return theme.obtainStyledAttributes(intArrayOf(id))
        .getColor(0, -1) != -1
}