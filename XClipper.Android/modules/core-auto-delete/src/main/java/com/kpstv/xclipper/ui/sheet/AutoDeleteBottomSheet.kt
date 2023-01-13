package com.kpstv.xclipper.ui.sheet

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.kpstv.xclipper.auto_delete.R
import com.kpstv.xclipper.auto_delete.databinding.BottomSheetAutoDeleteBinding
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.collectIn
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class AutoDeleteBottomSheet : CustomRoundedBottomSheetFragment(R.layout.bottom_sheet_auto_delete) {

    @Inject
    lateinit var appSettings: AppSettings

    private val binding by viewBinding(BottomSheetAutoDeleteBinding::bind)

    private var autoDeleteDayNumberFlow = MutableStateFlow<Int>(1)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swEnable.isChecked = appSettings.canAutoDeleteClips()
        binding.swEnable.setOnCheckedChangeListener { _, _ ->
            updateConfigLayout()
        }

        binding.cbDeleteRemote.isChecked = appSettings.shouldAutoDeleteRemoteClips()

        binding.npDays.value = appSettings.getAutoDeleteDayNumber()
        binding.npDays.setOnValueChangedListener { _, _, value ->
            updateAutoDeleteNumber(value)
        }

        observeAutoDeleteNumberFlow()

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            saveOptions()
        }
    }

    private fun saveOptions() {
        appSettings.setAutoDeleteClips(binding.swEnable.isChecked)
        appSettings.setShouldAutoDeleteRemoteClips(binding.cbDeleteRemote.isChecked)
        appSettings.setAutoDeleteDayNumber(autoDeleteDayNumberFlow.value)
    }

    private fun updateAutoDeleteNumber(value: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            autoDeleteDayNumberFlow.emit(value)
        }
    }

    private fun observeAutoDeleteNumberFlow() {
        // so that we don't make getString calls every millisecond
        autoDeleteDayNumberFlow.debounce(300L)
            .distinctUntilChanged()
            .collectIn(viewLifecycleOwner) { value ->
                binding.tvSummary.text = getString(R.string.ad_sheet_summary, value)
                binding.tvInfo.text = HtmlCompat.fromHtml(
                    getString(R.string.ad_sheet_info, value),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            }
    }

    private fun updateConfigLayout() {
        val isChecked = binding.swEnable.isChecked
        if (isChecked) {
            TransitionManager.beginDelayedTransition(binding.root, Fade())
            binding.mainLayout.show()
        } else {
            binding.mainLayout.collapse()
        }
    }

}