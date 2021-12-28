package com.kpstv.xclipper

import android.content.Context
import com.kpstv.xclipper.core_addons.R
import com.kpstv.xclipper.data.model.ExtensionItem
import com.kpstv.xclipper.extensions.colorFrom

object AddOns {
    fun getPinExtension(context: Context) : ExtensionItem = with(context) {
        return ExtensionItem(
            title = getString(R.string.pin_title),
            smallDescription = getString(R.string.pin_short_desc),
            fullDescription = getString(R.string.pin_full_desc),
            icon = R.drawable.pin_ic_lock_outline,
            dominantColor = context.colorFrom(R.color.pin_color),
            sku = getString(R.string.pin_sku)
        )
    }
    fun getAllExtensions(context: Context) : List<ExtensionItem> = with(context) {
        return listOf(
            getPinExtension(this)
        )
    }
}