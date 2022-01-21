package com.kpstv.xclipper.ui.fragments.sheets

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.text.HtmlCompat
import androidx.core.text.buildSpannedString
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.BottomSheetDisclosureBinding
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.viewmodel.DisclosureSheetViewModel
import com.kpstv.xclipper.ui.viewmodel.DisclosureState
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import io.noties.markwon.Markwon
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@AndroidEntryPoint
class DisclosureSheet : CustomRoundedBottomSheetFragment(R.layout.bottom_sheet_disclosure) {

    private val binding by viewBinding(BottomSheetDisclosureBinding::bind)
    private val viewModel by viewModels<DisclosureSheetViewModel>()

    @Inject lateinit var appSettings: AppSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.tvAgreement.movementMethod = LinkMovementMethod()
        binding.cbAgreement.text = HtmlCompat.fromHtml(getString(R.string.terms_check_agreement), HtmlCompat.FROM_HTML_MODE_COMPACT)
        binding.cbPrivacy.text = HtmlCompat.fromHtml(getString(R.string.terms_check_privacy_policy), HtmlCompat.FROM_HTML_MODE_COMPACT)

        val backgroundColor = requireContext().getColorAttr(R.attr.background)
        binding.bottomBanner.setBackgroundColor(ColorUtils.setAlphaComponent(backgroundColor, 230))

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.agreementCheckedMutableState.collect { binding.cbAgreement.isChecked = it }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.privacyCheckedMutableState.collect { binding.cbPrivacy.isChecked = it }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.acceptState.collect { binding.btnAccept.isEnabled = it }
        }

        binding.cbAgreement.setOnCheckedChangeListener { _, isChecked -> viewModel.agreementCheckedMutableState.value = isChecked  }
        binding.cbPrivacy.setOnCheckedChangeListener { _, isChecked -> viewModel.privacyCheckedMutableState.value = isChecked  }

        binding.btnDecline.setOnClickListener { requireActivity().finishAffinity() }
        binding.btnAccept.setOnClickListener {
            appSettings.setDisclosureAgreementShown(true)
            dismiss()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fetchPolicy().collect { state ->
                if (state is DisclosureState.Loading) {
                    binding.progressBar.show()
                } else {
                    binding.progressBar.hide()
                }
                when(state) {
                    is DisclosureState.UpdatePolicy -> {
                        update(state.data, state.lastUpdated)
                    }
                    else -> {
                        // show cached raw privacy policy
                        val policyText = resources.openRawResource(R.raw.policy).bufferedReader().readText()
                        update(policyText = policyText)
                        Logger.w(IllegalStateException(getString(R.string.err_privacy_policy)), null)
                        Toasty.error(requireContext(), getString(R.string.err_privacy_policy)).show()
                    }
                }
            }
        }
    }

    private fun update(policyText: String, lastUpdated: String = "unknown") {
        val spanned = Markwon.create(requireContext()).toMarkdown(policyText)
        binding.tvAgreement.text = buildSpannedString {
            appendLine()
            append(spanned)
            repeat(10) { appendLine() }
        }
        binding.tvUpdate.text = getString(R.string.terms_last_updated, lastUpdated)
    }
}