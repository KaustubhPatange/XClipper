package com.kpstv.xclipper.ui.fragments.settings

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.AUTO_SYNC_PREF
import com.kpstv.xclipper.App.BIND_DELETE_PREF
import com.kpstv.xclipper.App.BIND_PREF
import com.kpstv.xclipper.App.CONNECT_PREF
import com.kpstv.xclipper.App.FORCE_REMOVE_PREF
import com.kpstv.xclipper.App.LOGOUT_PREF
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.utils.Utils.Companion.logoutFromDatabase
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showConnectDialog
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showConnectionDialog
import com.kpstv.xclipper.ui.fragments.AnimatePreferenceFragment
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import javax.inject.Inject

@AndroidEntryPoint
class AccountPreference : AnimatePreferenceFragment() {

    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    @Inject
    lateinit var dbConnectionProvider: DBConnectionProvider

    private val mainViewModel: MainViewModel by viewModels()

    private var autoSyncPreference: SwitchPreferenceCompat? = null
    private var bindPreference: SwitchPreferenceCompat? = null
    private var bindDeletePreference: SwitchPreferenceCompat? = null
    private var logPreference: Preference? = null
    private var connectPreference: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_pref, rootKey)

        bindUI()

        logPreference = findPreference(LOGOUT_PREF)
        logPreference?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
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
        autoSyncPreference?.setOnPreferenceChangeListener { _, newValue ->
            App.runAutoSync = newValue as Boolean
            true
        }

        /** Bind delete preference */
        bindDeletePreference = findPreference(BIND_DELETE_PREF)
        bindDeletePreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.warning))
                    .setCancelable(false)
                    .setMessage(getString(R.string.bind_delete_warning))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        App.bindDelete = newValue as Boolean
                    }
                    .setNeutralButton(R.string.cancel) { _, _ ->
                        bindDeletePreference?.isChecked = false
                    }
                    .show()
            }
            true
        }

        /** Force logout preference */
        findPreference<Preference>(FORCE_REMOVE_PREF)?.setOnPreferenceClickListener {
            logoutFromDatabase(
                context = requireContext(),
                preferenceProvider = preferenceProvider,
                dbConnectionProvider = dbConnectionProvider
            )
            Toasty.info(requireContext(), getString(R.string.force_logout_text)).show()
            true
        }

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
            bindDeletePreference?.isEnabled = false
        } else {
            // Do not add bindDeletePreference as an auto property
            // since it will cause issues for non-paid users.
            connectPreference?.isEnabled = false
            logPreference?.isEnabled = true
            bindPreference?.isEnabled = true
            autoSyncPreference?.isEnabled = true
            bindDeletePreference?.isEnabled = true

            bindPreference?.isChecked = App.bindToFirebase
            autoSyncPreference?.isChecked = App.runAutoSync
        }
    }
}