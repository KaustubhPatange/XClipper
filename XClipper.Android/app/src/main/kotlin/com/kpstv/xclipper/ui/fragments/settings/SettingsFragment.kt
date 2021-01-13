package com.kpstv.xclipper.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.databinding.RecyclerViewBinding
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.activities.Settings
import com.kpstv.xclipper.ui.adapters.MenuAdapter
import com.kpstv.xclipper.ui.fragments.AnimateFragment

class SettingsFragment : AnimateFragment(R.layout.recycler_view) {
    private val binding by viewBinding(RecyclerViewBinding::bind)

    lateinit var listener: (String, String) -> Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = ArrayList<SpecialMenu>()

        /** General Setting */
        list.add(
            SpecialMenu(title = getString(R.string.service), image = R.drawable.ic_service) {
                listener.invoke(Settings.GENERAL_PREF, getString(R.string.service))
            }
        )

        /** Sync Setting */
        list.add(
            SpecialMenu(title = getString(R.string.account), image = R.drawable.ic_account) {
                listener.invoke(Settings.ACCOUNT_PREF, getString(R.string.account))
            }
        )

        /** Look & Feel Setting */
        list.add(
            SpecialMenu(title = getString(R.string.look_feel), image = R.drawable.ic_looks) {
                listener.invoke(Settings.LOOK_FEEL_PREF, getString(R.string.look_feel))
            }
        )

        /** Backup Setting */
        list.add(
            SpecialMenu(title = getString(R.string.backup), image = R.drawable.ic_backup) {
                listener.invoke(Settings.BACKUP_PREF, getString(R.string.backup))
            }
        )

        /** Upgrade Menu */
        list.add(
            SpecialMenu(
                title = getString(R.string.upgrade),
                image = R.drawable.ic_upgrade,
                imageTint = R.color.palette5,
                textColor = R.color.palette5
            ) { listener.invoke(Settings.UPGRADE_PREF, getString(R.string.upgrade)) }
        )

        /** About Menu */
        list.add(
            SpecialMenu(title = getString(R.string.about), image = R.drawable.ic_info) {
                listener.invoke(Settings.ABOUT_PREF, getString(R.string.about))
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = MenuAdapter(list, R.layout.item_settings)
        binding.recyclerView.setHasFixedSize(true)
    }
}