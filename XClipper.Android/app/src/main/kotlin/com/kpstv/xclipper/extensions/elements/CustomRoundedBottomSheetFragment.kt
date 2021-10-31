package com.kpstv.xclipper.extensions.elements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.view.ContextThemeWrapper
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment

open class CustomRoundedBottomSheetFragment(@LayoutRes private val layoutId: Int) : RoundedBottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(ContextThemeWrapper(requireContext(), requireActivity().theme)).inflate(layoutId, container)
    }
}