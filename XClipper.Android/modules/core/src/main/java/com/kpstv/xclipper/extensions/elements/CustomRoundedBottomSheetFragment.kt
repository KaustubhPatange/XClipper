package com.kpstv.xclipper.extensions.elements

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.view.ContextThemeWrapper
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment
import com.kpstv.xclipper.extensions.SimpleFunction

fun interface SheetDismissListener {
    fun onDismiss()
}

open class CustomRoundedBottomSheetFragment(@LayoutRes private val layoutId: Int) : RoundedBottomSheetDialogFragment() {
    private val dismissListeners = ArrayList<SheetDismissListener>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(ContextThemeWrapper(requireContext(), requireActivity().theme)).inflate(layoutId, container)
    }

    fun addOnDismissListener(listener: SheetDismissListener) {
        dismissListeners.add(listener)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListeners.forEach { it.onDismiss() }
    }

    override fun onDestroy() {
        dismissListeners.clear()
        super.onDestroy()
    }
}