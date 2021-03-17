package com.kpstv.xclipper.ui.fragments.sheets

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.SpecialHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper

class MoreBottomSheet(
    private val tinyUrlApiHelper: TinyUrlApiHelper,
    private val dictionaryApiHelper: DictionaryApiHelper,
    private val supportFragmentManager: FragmentManager,
    private val clipboardProvider: ClipboardProvider,
    private val onClose: () -> Unit = {},
    private val clip: Clip
) : RoundedBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        with(inflater.inflate(R.layout.bottom_sheet_more, container, false)) {

            SpecialHelper(
                context = context,
                dictionaryApiHelper = dictionaryApiHelper,
                tinyUrlApiHelper = tinyUrlApiHelper,
                clipboardProvider = clipboardProvider,
                supportFragmentManager = supportFragmentManager,
                lifecycleScope = viewLifecycleOwner.lifecycleScope,
                clip = clip
            ).setActions(this) {
                dismiss()
            }

            return this
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClose.invoke()
    }
}