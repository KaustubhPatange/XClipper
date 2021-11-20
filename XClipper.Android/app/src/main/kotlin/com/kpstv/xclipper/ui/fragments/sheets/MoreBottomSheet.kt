package com.kpstv.xclipper.ui.fragments.sheets

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.BottomSheetMoreBinding
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.helpers.specials.SpecialHelper
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@AndroidEntryPoint
class MoreBottomSheet(
    private val supportFragmentManager: FragmentManager,
    private val onClose: () -> Unit = {},
    private val clip: Clip
) : CustomRoundedBottomSheetFragment(R.layout.bottom_sheet_more) {

    private val binding by viewBinding(BottomSheetMoreBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SpecialHelper(
            context = requireContext(),
            supportFragmentManager = supportFragmentManager,
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            clip = clip
        ).setActions(binding) {
            dismiss()
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
                showDisclosureDialog(
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

        private fun showDisclosureDialog(context: Context, @StringRes message: Int, onAccept: () -> Unit, onDeny: () -> Unit = {}) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.disclosure)
                .setMessage(message)
                .setPositiveButton(R.string.accept) { _, _ -> onAccept()}
                .setNegativeButton(R.string.deny) { _, _ ->
                    Toasty.error(context, context.getString(R.string.disclosure_deny)).show()
                    onDeny()
                }
                .setCancelable(false)
                .show()
        }
    }
}