package com.kpstv.xclipper.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.navigation.ValueFragment
import com.kpstv.xclipper.data.model.SingleMenuItem
import com.kpstv.xclipper.di.SettingScreenHandler
import com.kpstv.xclipper.di.improve_detection.ImproveDetectionQuickTip
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.feature_settings.databinding.FragmentSettingsMainBinding
import com.kpstv.xclipper.ui.adapters.SingleMenuAdapter
import com.kpstv.xclipper.ui.helpers.AccessibilityQuickTipHelper
import com.kpstv.xclipper.ui.viewmodel.SettingNavViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : ValueFragment(R.layout.fragment_settings_main) {
    private val binding by viewBinding(FragmentSettingsMainBinding::bind)
    private val navViewModel by viewModels<SettingNavViewModel>(
        ownerProducer = ::requireParentFragment
    )

    @Inject lateinit var settingScreenHandler: SettingScreenHandler
    @Inject lateinit var improveDetectionQuickTip: ImproveDetectionQuickTip

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = ArrayList<SingleMenuItem>()

        settingScreenHandler.getAll().forEach { definition ->
            val element = when {
                settingScreenHandler.isUpgradeScreen(definition) -> SingleMenuItem(
                    title = getString(R.string.upgrade),
                    image = R.drawable.ic_upgrade,
                    imageTint = R.color.upgrade_symbol,
                    textColor = R.color.upgrade_symbol,
                    onClick = { navViewModel.navigateTo(definition) }
                )
                else -> SingleMenuItem(
                    title = getString(definition.titleRes),
                    image = definition.drawableRes,
                    onClick = { navViewModel.navigateTo(definition) }
                )
            }
            list.add(element)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = SingleMenuAdapter(list, R.layout.item_settings)
        binding.recyclerView.setHasFixedSize(true)

        setUpQuickTips()
    }

    private fun setUpQuickTips() {
        /* Clipboard Service tip */
        AccessibilityQuickTipHelper.addQuickTip(binding.tipContainer) {
            navViewModel.navigateTo(
                screenDefinition = settingScreenHandler.screenGeneral(),
                args = GeneralPreference.Args(highlightAccessibilityService = true)
            )
        }

        /* Improve detection tip */
        improveDetectionQuickTip.add(binding.tipContainer) {
            navViewModel.navigateTo(
                screenDefinition = settingScreenHandler.screenGeneral(),
                args = GeneralPreference.Args(highlightImproveDetection = true)
            )
        }
    }
}