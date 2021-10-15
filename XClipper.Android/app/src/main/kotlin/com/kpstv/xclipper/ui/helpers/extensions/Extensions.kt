package com.kpstv.xclipper.ui.helpers.extensions

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.SimpleFunction
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
    fun getPinExtension(context: Context) : ExtensionItem = with(context) {
        return ExtensionItem(
            title = getString(R.string.pin_title),
            smallDescription = getString(R.string.pin_short_desc),
            fullDescription = getString(R.string.pin_full_desc),
            icon = R.drawable.ic_lock_outline,
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

object AddOnsHelper {
    fun getHelperForPinLock(context: Context, preferenceProvider: PreferenceProvider) : ExtensionHelper {
        return ExtensionHelper(context, preferenceProvider, context.getString(R.string.pin_sku))
    }

    fun showExtensionDialog(context: Context, onClick: SimpleFunction) {
        MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.ic_crown_colored)
            .setTitle(R.string.extension_title)
            .setMessage(R.string.extension_message)
            .setPositiveButton(R.string.activate) { _, _ -> onClick() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}