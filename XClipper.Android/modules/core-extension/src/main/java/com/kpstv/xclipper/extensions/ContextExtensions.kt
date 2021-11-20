package com.kpstv.xclipper.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.color.MaterialColors

fun Context.layoutInflater(): LayoutInflater = LayoutInflater.from(this)

fun Context.colorFrom(@ColorRes color: Int) =
    ContextCompat.getColor(this, color)

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

fun Context.toPx(dp: Int) : Float = (resources.displayMetrics.density * dp)

@ColorInt
fun Context.getColorAttr(@AttrRes id: Int, @ColorInt fallbackColor: Int = 0): Int {
    return MaterialColors.getColor(this, id, fallbackColor)
}

fun Context.getAttrResourceId(@AttrRes id: Int, typedValue: TypedValue = TypedValue()) : Int {
    theme?.resolveAttribute(id, typedValue, true)
    return typedValue.resourceId
}

fun Context.getRawDataAttr(@AttrRes id: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true) : Int {
    theme.resolveAttribute(id, typedValue, resolveRefs)
    return typedValue.data
}

fun Context.broadcastManager() = LocalBroadcastManager.getInstance(this)