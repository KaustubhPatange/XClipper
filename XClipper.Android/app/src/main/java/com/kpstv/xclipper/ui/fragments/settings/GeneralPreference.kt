package com.kpstv.xclipper.ui.fragments.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.BIND_PREF
import com.kpstv.xclipper.App.BLACKLIST_PREF
import com.kpstv.xclipper.App.DICTIONARY_LANGUAGE
import com.kpstv.xclipper.App.LANG_PREF
import com.kpstv.xclipper.App.SERVICE_PREF
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isAccessibilityServiceEnabled
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isClipboardAccessibilityServiceRunning
import com.kpstv.xclipper.extensions.utils.Utils.Companion.openAccessibility
import com.kpstv.xclipper.extensions.utils.Utils.Companion.retrievePackageList
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showAccessibilityDialog
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showConnectDialog
import com.kpstv.xclipper.service.ClipboardAccessibilityService


class GeneralPreference : PreferenceFragmentCompat() {
    private val TAG = javaClass.simpleName
    private var checkPreference: SwitchPreferenceCompat? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_pref, rootKey)

        /** Load app list */
        retrievePackageList(requireContext())

        /** Black list app preference */
        val blacklistPreference = findPreference<MultiSelectListPreference>(BLACKLIST_PREF)

        blacklistPreference?.entries = App.appList.mapNotNull { it.label }.toTypedArray()
        blacklistPreference?.entryValues = App.appList.mapNotNull { it.packageName }.toTypedArray()

        blacklistPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is Set<*>) {
                App.blackListedApps = newValue as Set<String>
            }
            true
        }


        /** Clipboard Service preference */
        checkPreference = findPreference(SERVICE_PREF)
        checkPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean)
                showAccessibilityDialog(requireContext()) {
                    checkForService()
                }
            else openAccessibility(requireContext())
            true
        }

        /** Language code preference */
        findPreference<ListPreference>(LANG_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            DICTIONARY_LANGUAGE = newValue as String
            true
        }
    }


    override fun onResume() {
        checkForService()
        super.onResume()
    }

    private fun checkForService() {
        checkPreference?.isChecked = isClipboardAccessibilityServiceRunning(requireContext())
    }
}