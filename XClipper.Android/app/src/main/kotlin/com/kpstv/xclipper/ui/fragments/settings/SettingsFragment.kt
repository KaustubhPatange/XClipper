package com.kpstv.xclipper.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.navigation.ValueFragment
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.FragmentSettingsMainBinding
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.fragments.Settings
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.fragments.ImproveDetectionHelper
import com.kpstv.xclipper.ui.viewmodels.SettingNavViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : ValueFragment(R.layout.fragment_settings_main) {
    private val binding by viewBinding(FragmentSettingsMainBinding::bind)
    private val navViewModel by viewModels<SettingNavViewModel>(
        ownerProducer = ::requireParentFragment
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = ArrayList<SpecialMenu>()

        /** General Setting */
        list.add(
            SpecialMenu(title = getString(R.string.service), image = R.drawable.ic_service) {
                navViewModel.navigateTo(Settings.Screen.GENERAL)
            }
        )

        /** Sync Setting */
        list.add(
            SpecialMenu(title = getString(R.string.account), image = R.drawable.ic_sync) {
                navViewModel.navigateTo(Settings.Screen.ACCOUNT)
            }
        )

        /** Look & Feel Setting */
        list.add(
            SpecialMenu(title = getString(R.string.look_feel), image = R.drawable.ic_looks) {
                navViewModel.navigateTo(Settings.Screen.LOOK_FEEL)
            }
        )

        /** Backup Setting */
        list.add(
            SpecialMenu(title = getString(R.string.backup), image = R.drawable.ic_backup) {
                navViewModel.navigateTo(Settings.Screen.BACKUP)
            }
        )

        /** Upgrade Menu */
        list.add(
            SpecialMenu(
                title = getString(R.string.upgrade),
                image = R.drawable.ic_upgrade,
                imageTint = R.color.palette5,
                textColor = R.color.palette5
            ) { navViewModel.navigateTo(Settings.Screen.UPGRADE) }
        )

        /** About Menu */
        list.add(
            SpecialMenu(title = getString(R.string.about), image = R.drawable.ic_info) {
                navViewModel.navigateTo(Settings.Screen.ABOUT)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = MenuAdapter(list, R.layout.item_settings)
        binding.recyclerView.setHasFixedSize(true)

        setUpQuickTips()
    }

    private fun setUpQuickTips() {
        /* Improve detection tip */
        ImproveDetectionHelper.addQuickTip(binding.tipContainer) {
            navViewModel.navigateTo(Settings.Screen.GENERAL, GeneralPreference.Args(highlightImproveDetection = true))
        }
    }
}