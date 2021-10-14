package com.kpstv.xclipper.ui.helpers.extensions

import android.content.Context
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.colorFrom

data class ExtensionItem(
    override val title: String,
    override val fullDescription: String,
    override val icon: Int,
    override val dominantColor: Int,
    override val sku: String,
    override val smallDescription: String
) : ExtensionData

object AddOns {
    fun getAllExtensions(context: Context) : List<ExtensionItem> = with(context) {
        return listOf(
            ExtensionItem(
                title = getString(R.string.pin_title),
                smallDescription = getString(R.string.pin_short_desc),
                fullDescription = getString(R.string.pin_full_desc),
                icon = R.drawable.ic_lock_outline,
                dominantColor = context.colorFrom(R.color.pin_color),
                sku = getString(R.string.pin_sku)
            )
        )
    }
}