package com.kpstv.xclipper.ui.helpers

import android.content.Context
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.AddOns
import com.kpstv.xclipper.PinLockHelper
import com.kpstv.xclipper.core_addons.R
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.drawableFrom
import com.kpstv.xclipper.extensions.toPx

object AddOnsHelper {
    fun getHelperForPinLock(context: Context) : ExtensionHelper {
        return ExtensionHelper(context, context.getString(R.string.pin_sku))
    }

    fun showExtensionDialog(context: Context, onClick: SimpleFunction) {
        MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.addons_ic_crown_colored)
            .setTitle(R.string.extension_title)
            .setMessage(R.string.extension_message)
            .setPositiveButton(R.string.activate) { _, _ -> onClick() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun addPremiumIcon(textView: TextView) {
        val drawable = textView.context.drawableFrom(R.drawable.addons_ic_crown_colored)
        drawable!!.setBounds(0, 0, textView.height, textView.height)
        textView.setCompoundDrawables(null, null, drawable, null)
        textView.compoundDrawablePadding = textView.context.toPx(10).toInt()
    }

    fun removePremiumIcon(textView: TextView) {
        textView.setCompoundDrawables(null, null, null, null)
    }

    suspend fun verifyExtensions(context: Context) {
        val lists = AddOns.getAllExtensions(context)
        lists.forEach { item ->
            val helper = ExtensionHelper.BillingHelper(context, item.sku)
            if (helper.init()) { // auto check for validation.
                when(item) {
                    AddOns.getPinExtension(context) -> {
                        PinLockHelper.internalRemoveAppLock(context)
                    }
                }
            }
        }
    }
}