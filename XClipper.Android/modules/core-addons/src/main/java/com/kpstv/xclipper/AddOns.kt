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
    fun getCustomizeThemeExtension(context: Context) : ExtensionItem = with(context) {
        return ExtensionItem(
            title = getString(R.string.ct_title),
            smallDescription = getString(R.string.ct_short_desc),
            fullDescription = getString(R.string.ct_full_desc),
            icon = R.drawable.ic_palette,
            dominantColor = context.colorFrom(R.color.colorPalette),
            sku = getString(R.string.ct_sku)
        )
    }
    fun getAutoDeleteExtension(context: Context) : ExtensionItem = with(context) {
        return ExtensionItem(
            title = getString(R.string.ad_title),
            smallDescription = getString(R.string.ad_short_desc),
            fullDescription = getString(R.string.ad_full_desc),
            icon = R.drawable.ic_delete_outlined,
            dominantColor = context.colorFrom(R.color.magenta),
            sku = getString(R.string.ad_sku)
        )
    }
    fun getAllExtensions(context: Context) : List<ExtensionItem> = with(context) {
        return listOf(
            getAutoDeleteExtension(this),
            getCustomizeThemeExtension(this),
            getPinExtension(this),
        )
    }
}