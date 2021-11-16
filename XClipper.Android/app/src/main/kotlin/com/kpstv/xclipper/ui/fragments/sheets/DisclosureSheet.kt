package com.kpstv.xclipper.ui.fragments.sheets

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.text.HtmlCompat
import androidx.core.text.buildSpannedString
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.BottomSheetDisclosureBinding
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
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
                    DisclosureState.EmptyPolicy -> {
                        Toasty.error(requireContext(), getString(R.string.err_privacy_policy)).show()
                    }
                    is DisclosureState.UpdatePolicy -> {
                        val spanned = Markwon.create(requireContext()).toMarkdown(state.data)
                        binding.tvAgreement.text = buildSpannedString {
                            appendLine()
                            append(spanned)
                            repeat(10) { appendLine() }
                        }
                        binding.tvUpdate.text = getString(R.string.terms_last_updated, state.lastUpdated)
                    }
                    else -> {}
                }
            }
        }
    }
}

@HiltViewModel
class DisclosureSheetViewModel @Inject constructor(
    private val retrofitUtils: RetrofitUtils
) : ViewModel() {
    val privacyCheckedMutableState = MutableStateFlow(false)
    val agreementCheckedMutableState = MutableStateFlow(false)

    val acceptState: Flow<Boolean> =
        privacyCheckedMutableState.combine(agreementCheckedMutableState) { a, b -> a && b}

    internal fun fetchPolicy() : Flow<DisclosureState> = flow {
        val result = retrofitUtils.fetch(POLICY)
        result.onFailure { emit(DisclosureState.EmptyPolicy) }
        result.onSuccess {
            val body = it.body?.string() ?: run { emit(DisclosureState.EmptyPolicy); return@onSuccess }
            val date = UPDATED_DATE_PATTERN.toRegex().find(body)?.groupValues?.get(1)
            if (date != null) {
                emit(DisclosureState.UpdatePolicy(body, date))
            } else {
                emit(DisclosureState.UpdatePolicy(body))
            }
        }
    }

    private companion object {
        private const val UPDATED_DATE_PATTERN = "Updated:\\s?([\\d]{2}/[\\d]{2}/[\\d]{2,4})"

        private const val POLICY = "https://raw.githubusercontent.com/KaustubhPatange/XClipper/master/XClipper.Android/POLICY.md"
    }
}

internal sealed class DisclosureState {
    object Loading : DisclosureState()
    data class UpdatePolicy(val data: String, val lastUpdated: String) : DisclosureState() {
        constructor(data: String) : this(data = data, lastUpdated = "unknown")
    }
    object EmptyPolicy : DisclosureState()
}