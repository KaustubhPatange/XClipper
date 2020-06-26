package com.kpstv.xclipper.ui.fragments.settings

import android.app.AlertDialog
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.AUTO_SYNC_PREF
import com.kpstv.xclipper.App.BIND_PREF
import com.kpstv.xclipper.App.CONNECT_PREF
import com.kpstv.xclipper.App.LOGOUT_PREF
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showConnectDialog
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showConnectionDialog
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import com.kpstv.xclipper.ui.viewmodels.MainViewModelFactory
import es.dmoral.toasty.Toasty
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class AccountPreference() : PreferenceFragmentCompat(), KodeinAware {

    override val kodein by kodein()
    private val preferenceProvider by instance<PreferenceProvider>()
    private val viewModelFactory by instance<MainViewModelFactory>()
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private var autoSyncPreference: SwitchPreferenceCompat? = null
    private var bindPreference: SwitchPreferenceCompat? = null
    private var logPreference: Preference? = null
    private var connectPreference: Preference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_pref, rootKey)

        bindUI()

        logPreference = findPreference(LOGOUT_PREF)
        logPreference?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.alert))
                .setMessage(getString(R.string.logout_msg))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->

                    val dialog = showConnectionDialog(requireContext())

                    mainViewModel.removeDeviceConnection(ResponseListener(
                        complete = {
                            dialog.dismiss()
                            bindPreference?.isChecked = false
                            autoSyncPreference?.isChecked = false
                            Toasty.info(requireContext(), getString(R.string.logout_success)).show()
                        },
                        error = {
                            dialog.dismiss()
                            Toasty.error(requireContext(), it.message!!).show()
                        }
                    ))
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
            true
        }

        /** Connect Preference */
        connectPreference = findPreference(CONNECT_PREF)
        connectPreference?.setOnPreferenceClickListener {
            if (UID.isBlank())
                showConnectDialog(requireActivity())
            else
                Toasty.info(requireContext(), getString(R.string.connection_exist)).show()
            true
        }

        /** Database binding preference */
        bindPreference = findPreference(BIND_PREF)
        bindPreference?.setOnPreferenceChangeListener { _, newValue ->
            App.bindToFirebase = newValue as Boolean
            true
        }

        /** Auto sync preference */
        autoSyncPreference = findPreference(AUTO_SYNC_PREF)
    }

    override fun onResume() {
        super.onResume()
        checkForPreferenceChanged()
    }

    private fun bindUI() {
        preferenceProvider.observePreference()
        { _, s ->
            checkForPreferenceChanged()
        }
    }

    private fun checkForPreferenceChanged() {
        if (UID.isBlank()) {
            connectPreference?.isEnabled = true
            logPreference?.isEnabled = false
            bindPreference?.isEnabled = false
            autoSyncPreference?.isEnabled = false
        } else {
            connectPreference?.isEnabled = false
            logPreference?.isEnabled = true
            bindPreference?.isEnabled = true
            autoSyncPreference?.isEnabled = true

            bindPreference?.isChecked = App.bindToFirebase
            autoSyncPreference?.isChecked = App.runAutoSync
        }
    }
}