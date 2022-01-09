package com.kpstv.xclipper.ui.fragments.sheets

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_special.R
import com.kpstv.xclipper.feature_special.databinding.BottomSheetSpecialBinding
import com.kpstv.xclipper.ui.helpers.SpecialHelper
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class SpecialBottomSheet : CustomRoundedBottomSheetFragment(R.layout.bottom_sheet_special) {

    private val binding by viewBinding(BottomSheetSpecialBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.getParcelable<Args>(ARG_CLIP) ?: run { dismiss(); return }

        SpecialHelper(
            context = requireContext(),
            supportFragmentManager = requireFragmentManager(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            clip = args.clip
        ).setActions(binding) {
            dismiss()
        }
    }

    @Parcelize
    data class Args(val clipJson: String) : Parcelable {
        val clip: Clip get() = Clip.fromJson(clipJson)
        companion object {
            fun fromClip(clip: Clip) = Args(clipJson = clip.toJson())
        }
    }

    companion object {
        private const val ARG_CLIP = "com.kpstv.xclipper:arg:clip_json"
        private const val DICTIONARY_DIALOG = "dictionary_dialog"
        private fun PreferenceProvider.isDialogShown(): Boolean {
            return getBooleanKey(DICTIONARY_DIALOG, false)
        }
        private fun PreferenceProvider.setDialogShown(value: Boolean) {
            putBooleanKey(DICTIONARY_DIALOG, value)
        }

        fun show(clip: Clip, activity: FragmentActivity, onClose: () -> Unit = {}) {
            show(clip, activity, activity.supportFragmentManager, onClose)
        }

        fun show(clip: Clip, fragment: Fragment, onClose: () -> Unit = {}) {
            show(clip, fragment.requireContext(), fragment.childFragmentManager, onClose)
        }

        private fun show(clip: Clip, context: Context, fragmentManager: FragmentManager, onClose: () -> Unit = {}) {
            val preferenceProvider = CommonReusableEntryPoints.get(context).preferenceProvider()

            fun showSheet() {
                val sheet = SpecialBottomSheet().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_CLIP, Args.fromClip(clip))
                    }
                }
                sheet.showNow(fragmentManager, "blank")
                sheet.dialog?.setOnDismissListener { onClose() }
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