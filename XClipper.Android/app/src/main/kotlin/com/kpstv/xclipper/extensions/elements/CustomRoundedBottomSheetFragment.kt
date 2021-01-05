package com.kpstv.xclipper.extensions.elements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment

open class CustomRoundedBottomSheetFragment(@LayoutRes private val layoutId: Int) : RoundedBottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }
}