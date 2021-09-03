package com.kpstv.xclipper.ui.fragments.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.BLACKLIST_PREF
import com.kpstv.xclipper.App.DICTIONARY_LANGUAGE
import com.kpstv.xclipper.App.IMAGE_MARKDOWN_PREF
import com.kpstv.xclipper.App.LANG_PREF
import com.kpstv.xclipper.App.SERVICE_PREF
import com.kpstv.xclipper.App.SUGGESTION_PREF
import com.kpstv.xclipper.App.SWIPE_DELETE_PREF
import com.kpstv.xclipper.App.TRIM_CLIP_PREF
import com.kpstv.xclipper.App.showSuggestion
import com.kpstv.xclipper.App.swipeToDelete
import com.kpstv.xclipper.App.trimClipText
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.DialogProgressBinding
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isClipboardAccessibilityServiceRunning
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isSystemOverlayEnabled
import com.kpstv.xclipper.extensions.utils.Utils.Companion.retrievePackageList
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showAccessibilityDialog
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showDisableAccessibilityDialog
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showOverlayDialog
import com.kpstv.xclipper.ui.dialogs.MultiSelectDialogBuilder
import com.kpstv.xclipper.ui.dialogs.MultiSelectModel3
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeneralPreference : PreferenceFragmentCompat() {
    private val TAG = javaClass.simpleName
    private var checkPreference: SwitchPreferenceCompat? = null
    private var overlayPreference: SwitchPreferenceCompat? = null

    @Inject lateinit var preferenceProvider: PreferenceProvider

    private var appsDialog: AlertDialog? = null

    /**
     * Since overlay permission makes you to leave the activity, the only way
     * to check the preference is to set a boolean and then in onResume() we
     * will set the preference.
     */
    private var rememberToCheckOverlaySwitch = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_pref, rootKey)

        /** Black list app preference */
        val blacklistPreference = findPreference<Preference>(BLACKLIST_PREF)

   /*     Coroutines.main {
            *//** Load installed app list *//*
            App.appList = retrievePackageList(requireContext())
            blacklistPreference?.entries = App.appList.mapNotNull { it.label }.toTypedArray()
            blacklistPreference?.entryValues = App.appList.mapNotNull { it.packageName }.toTypedArray()
        }*/

        blacklistPreference?.setOnPreferenceClickListener {
            showBlacklistAppDialog()
//            if (newValue is Set<*>) {
//                App.blackListedApps = newValue as Set<String>
//            }
            true
        }

        /** Show suggestion preference */
        overlayPreference = findPreference(SUGGESTION_PREF)
        overlayPreference?.setOnPreferenceChangeListener { _, newValue ->

            if (!isSystemOverlayEnabled(requireContext()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showOverlayDialog(requireContext())

                if (newValue == true) rememberToCheckOverlaySwitch = true

                return@setOnPreferenceChangeListener false
            }
            showSuggestion = newValue as Boolean
            true
        }

        /** Clipboard Service preference */
        checkPreference = findPreference(SERVICE_PREF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) // Add that BETA tag to summary for Android 10+ users
            checkPreference?.summary = "[BETA] ${checkPreference?.summary}"
        checkPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                showAccessibilityDialog(requireContext()) { checkForService() }
            } else {
                showDisableAccessibilityDialog(requireContext()) { checkForService() }
            }
            true
        }

        /** Swipe to delete preference */
        findPreference<SwitchPreferenceCompat>(SWIPE_DELETE_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            swipeToDelete = newValue as Boolean
            Toasty.info(requireContext(), getString(R.string.settings_restart)).show()
            true
        }

        /** Text trimming while displaying */
        findPreference<SwitchPreferenceCompat>(TRIM_CLIP_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            trimClipText = newValue as Boolean
            Toasty.info(requireContext(), getString(R.string.settings_restart)).show()
            true
        }

        /** Language code preference */
        findPreference<ListPreference>(LANG_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            DICTIONARY_LANGUAGE = newValue as String
            true
        }

        /** Reset onboarding screens **/
        findPreference<Preference>(RESET_PREF)?.setOnPreferenceClickListener {
            preferenceProvider.putBooleanKey(App.SHOW_SEARCH_FEATURE, true) // bubble search feature
            preferenceProvider.putBooleanKey(App.TUTORIAL_PREF, false)
            Toasty.info(requireContext(), getString(R.string.onboard_screens_reset)).show()
            true
        }

        /** Experimental Image loading */
        findPreference<SwitchPreferenceCompat>(IMAGE_MARKDOWN_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            App.LoadImageMarkdownText = newValue as Boolean
            true
        }
    }

    override fun onStart() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            localBroadcastReceiver, IntentFilter(ACTION_CHECK_PREFERENCES)
        )
        super.onStart()
    }

    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkForService()
        }
    }

    override fun onResume() {
        checkForService()
        super.onResume()
    }

    override fun onDestroyView() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(localBroadcastReceiver)
        super.onDestroyView()
    }

    private fun checkForService() {
        checkPreference?.isChecked = isClipboardAccessibilityServiceRunning(requireContext())
        if (rememberToCheckOverlaySwitch) {
            overlayPreference?.isChecked = isSystemOverlayEnabled(requireContext())
            rememberToCheckOverlaySwitch = false
            showSuggestion = true
        }
    }

    private fun showBlacklistAppDialog() {
        val job = SupervisorJob()
        CoroutineScope(Dispatchers.IO + job).launch {
            val apps = retrievePackageList(requireContext())
            lifecycleScope.launch {
                appsDialog?.dismiss()
                appsDialog = MultiSelectDialogBuilder(
                    context = requireContext(),
                    itemsCheckedState = { itemsCheckedState ->
                        val packages = itemsCheckedState.filter { it.value }.map { apps[it.key].packageName?.toString() }
                            .filterNotNull().toSet()
                        App.blackListedApps = packages
                        preferenceProvider.setStringSet(BLACKLIST_PREF, packages)
                    }
                ).apply {
                    setTitle(getString(R.string.blacklist_apps))
                    setItems(apps.map { pkg ->
                        MultiSelectModel3(
                            title = pkg.label.toString(),
                            subtitle = pkg.packageName.toString(),
                            isChecked = App.blackListedApps?.contains(pkg.packageName) ?: false
                        )
                    })
                }.create()
                appsDialog?.show()

                /*val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_multichoice, apps.map { it.packageName })
                appsDialog?.dismiss()
                appsDialog = MaterialAlertDialogBuilder(requireContext()).apply {
                    setAdapter(adapter) { _, index ->
                        val dialog = appsDialog ?: return@setAdapter
                        val checkedItems = dialog.listView.checkedItemPositions.valueIterator().asSequence().map { checked ->
                            if (!checked) return@map null
                            val packageName = apps[index].packageName?.toString() ?: return@map null
                            packageName
                        }.filterNotNull().toSet()
                        App.blackListedApps = checkedItems
                        preferenceProvider.setStringSet(BLACKLIST_PREF, checkedItems)
                    }
                }.create().apply {
                    listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
                    show()
                }*/
            }
        }
        appsDialog = MaterialAlertDialogBuilder(requireContext()).apply {
            val binding = DialogProgressBinding.inflate(requireContext().layoutInflater())
            binding.tvMessage.text = getString(R.string.load_apps)
            binding.button.text = getString(R.string.dismiss)
            binding.button.setOnClickListener {
                job.cancel()
                appsDialog?.dismiss()
            }
            setView(binding.root)
            setCancelable(false)
        }.create()
        appsDialog?.show()
    }


    companion object {
        const val ACTION_CHECK_PREFERENCES = "com.kpstv.xclipper.action_check_preferences"
        const val RESET_PREF = "reset_intro_pref"

        fun checkForSettings(context: Context) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_CHECK_PREFERENCES))
        }
    }
}