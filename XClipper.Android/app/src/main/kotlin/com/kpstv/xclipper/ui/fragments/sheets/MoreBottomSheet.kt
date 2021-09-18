package com.kpstv.xclipper.ui.fragments.sheets

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.dao.PreviewDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.SpecialHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreBottomSheet(
    private val supportFragmentManager: FragmentManager,
    private val onClose: () -> Unit = {},
    private val clip: Clip
) : RoundedBottomSheetDialogFragment() {

    @Inject lateinit var linkPreviewDao: PreviewDao
    @Inject lateinit var dictionaryApiHelper: DictionaryApiHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        with(inflater.inflate(R.layout.bottom_sheet_more, container, false)) {

            SpecialHelper(
                context = requireContext(),
                dictionaryApiHelper = dictionaryApiHelper,
                linkPreviewDao = linkPreviewDao,
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

    companion object {
        private const val DICTIONARY_DIALOG = "dictionary_dialog"
        private fun PreferenceProvider.isDialogShown(): Boolean {
            return getBooleanKey(DICTIONARY_DIALOG, false)
        }
        private fun PreferenceProvider.setDialogShown(value: Boolean) {
            putBooleanKey(DICTIONARY_DIALOG, value)
        }

        fun show(clip: Clip, activity: FragmentActivity, preferenceProvider: PreferenceProvider, onClose: () -> Unit = {}) {
            show(clip, activity, activity.supportFragmentManager, preferenceProvider, onClose)
        }

        fun show(clip: Clip, fragment: Fragment, preferenceProvider: PreferenceProvider, onClose: () -> Unit = {}) {
            show(clip, fragment.requireContext(), fragment.childFragmentManager, preferenceProvider, onClose)
        }

        private fun show(clip: Clip, context: Context, fragmentManager: FragmentManager, preferenceProvider: PreferenceProvider, onClose: () -> Unit = {}) {
            fun showSheet() {
               MoreBottomSheet(
                   supportFragmentManager = fragmentManager,
                   onClose = onClose,
                   clip = clip
               ).show(fragmentManager, "blank")
            }

            if (!preferenceProvider.isDialogShown()) {
                Utils.showDisclosureDialog(
                    context = context,
                    message = R.string.dictionary_disclosure,
                    onAccept = {
                        preferenceProvider.setDialogShown(true)
                        showSheet()
                    },
                    onDeny = onClose
                )
            } else {
                showSheet()
            }
        }
    }
}