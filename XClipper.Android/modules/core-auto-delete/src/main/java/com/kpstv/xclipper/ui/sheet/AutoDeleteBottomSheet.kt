package com.kpstv.xclipper.ui.sheet

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.chip.Chip
import com.kpstv.xclipper.auto_delete.R
import com.kpstv.xclipper.auto_delete.databinding.BottomSheetAutoDeleteBinding
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.collectIn
import com.kpstv.xclipper.extensions.drawableFrom
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.small
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

    private val autoDeleteSetting by lazy { appSettings.getAutoDeleteSetting() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swEnable.isChecked = appSettings.canAutoDeleteClips()
        binding.swEnable.setOnCheckedChangeListener { _, _ ->
            updateConfigLayout()
        }

        binding.cbDeleteRemote.isChecked = autoDeleteSetting.shouldDeleteRemoteClip
        binding.cbDeletePinned.isChecked = autoDeleteSetting.shouldDeletePinnedClip

        binding.npDays.value = autoDeleteSetting.dayNumber
        binding.npDays.setOnValueChangedListener { _, _, value ->
            updateAutoDeleteNumber(value)
        }

        updateConfigLayout()
        setupTagExcludeChipGroup()

        observeAutoDeleteNumberFlow()

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            saveOptions()
            dismiss()
        }
    }

    private fun saveOptions() {
        val excludeTags = binding.cgDeleteTags.checkedChipIds.map {
            binding.cgDeleteTags.findViewById<Chip>(it).text.toString()
        }.toSet()

        val updatedSettings = autoDeleteSetting.copy(
            shouldDeleteRemoteClip = binding.cbDeleteRemote.isChecked,
            shouldDeletePinnedClip = binding.cbDeletePinned.isChecked,
            dayNumber = autoDeleteDayNumberFlow.value,
            excludeTags = excludeTags
        )
        appSettings.setAutoDeleteSetting(updatedSettings)
        appSettings.setAutoDeleteClips(binding.swEnable.isChecked)
    }

    private fun updateAutoDeleteNumber(value: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            autoDeleteDayNumberFlow.emit(value)
        }
    }

    private fun setupTagExcludeChipGroup() {
        val excludeTags = autoDeleteSetting.excludeTags

        val foregroundColor =
            ColorStateList.valueOf(requireContext().getColorAttr(R.attr.colorForeground))
        ClipTag.values().forEach {
            binding.cgDeleteTags.addView(
                Chip(requireContext()).apply {
                    id = it.small().hashCode()
                    checkedIcon = drawableFrom(R.drawable.ic_check_white_24dp)
                    text = it.small()
                    chipBackgroundColor = foregroundColor
                    isCheckable = true
                    isChecked = excludeTags.contains(it.small())
                }
            )
        }
    }

    private fun observeAutoDeleteNumberFlow() {
        fun update(value: Int) {
            binding.tvSummary.text = getString(R.string.ad_sheet_summary, value)
            binding.tvInfo.text = HtmlCompat.fromHtml(
                getString(R.string.ad_sheet_info, value),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }

        update(autoDeleteDayNumberFlow.value)

        // so that we don't make getString calls every millisecond
        autoDeleteDayNumberFlow.debounce(300L)
            .distinctUntilChanged()
            .collectIn(viewLifecycleOwner) { update(it) }
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