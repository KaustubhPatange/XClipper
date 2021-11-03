package com.kpstv.xclipper.ui.fragments.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.getKeyArgs
import com.kpstv.navigation.hasKeyArgs
import com.kpstv.pin_lock.PinLockHelper
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.BLACKLIST_PREF
import com.kpstv.xclipper.App.DICTIONARY_LANGUAGE
import com.kpstv.xclipper.App.LANG_PREF
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
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isSystemOverlayEnabled
import com.kpstv.xclipper.extensions.utils.Utils.Companion.retrievePackageList
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showAccessibilityDialog
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showDisableAccessibilityDialog
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showOverlayDialog
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.service.helper.ClipboardLogDetector
import com.kpstv.xclipper.ui.dialogs.Dialogs
import com.kpstv.xclipper.ui.dialogs.MultiSelectDialogBuilder
import com.kpstv.xclipper.ui.dialogs.MultiSelectModel3
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.extensions.AddOns
import com.kpstv.xclipper.ui.helpers.extensions.AddOnsHelper
import com.kpstv.xclipper.ui.viewmodels.SettingNavViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class GeneralPreference : AbstractPreferenceFragment() {
    private val TAG = javaClass.simpleName
    private var checkPreference: SwitchPreferenceCompat? = null
    private var improveDetectPreference: SwitchPreferenceCompat? = null
    private var overlayPreference: SwitchPreferenceCompat? = null
    private var pinLockPreference: SwitchPreferenceCompat? = null

    @Inject lateinit var preferenceProvider: PreferenceProvider
    @Inject lateinit var appSettings: AppSettings

    private val settingsNavViewModel by viewModels<SettingNavViewModel>(ownerProducer = ::requireParentFragment)

    private var appsDialog: AlertDialog? = null

    /**
     * Since overlay permission makes you to leave the activity, the only way
     * to check the preference is to set a boolean and then in onResume() we
     * will set the preference.
     */
    private var rememberToCheckOverlaySwitch = false
    private var rememberToCheckForPinLock = false

    private val pinLockExtensionHelper by lazy { AddOnsHelper.getHelperForPinLock(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_pref, rootKey)

        /** Black list app preference */
        val blacklistPreference = findPreference<Preference>(BLACKLIST_PREF)

        blacklistPreference?.setOnPreferenceClickListener {
            showBlacklistAppDialog()
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

        /** Improve detection preference */
        improveDetectPreference = findPreference(ACTIVE_ADB_MODE_PREF)
        if (Build.VERSION.SDK_INT < 29) improveDetectPreference?.isEnabled = false
        improveDetectPreference?.setOnPreferenceChangeListener call@{ _, newValue ->
            if (newValue as Boolean) {
                val canDetect = ClipboardLogDetector.isDetectionCompatible(requireContext())
                if (!canDetect) {
                    preferenceProvider.putBooleanKey(TEMP_CHECK_IMPROVE_ON_START, true)
                    Dialogs.showImproveDetectionDialog(requireContext())
                    return@call canDetect
                }
            }
            appSettings.setImproveDetectionEnabled(newValue)
            true
        }

        /** Pin Lock preference */
        pinLockPreference = findPreference(PIN_LOCK_PREF)
        pinLockPreference?.setOnPreferenceChangeListener call@{ _, newValue ->
            if (pinLockExtensionHelper.isActive()) {
                val value = newValue as Boolean
                if (value && !PinLockHelper.isPinLockEnabled()) {
                    // trying to create a new pin
                    Dialogs.showPinLockInfoDialog(
                        context = requireContext(),
                        onPositive = {
                            PinLockHelper.createANewPinLock(requireContext())
                            rememberToCheckForPinLock = true
                        }
                    )
                } else if (!value && PinLockHelper.isPinLockEnabled()) {
                    // trying to disable it
                    PinLockHelper.disablePinLock(requireActivity())
                    rememberToCheckForPinLock = true
                } else if (value && PinLockHelper.isPinLockEnabled()) {
                    // must be error from our side so we should just set pin lock
                    return@call true
                }
            } else {
                AddOnsHelper.showExtensionDialog(
                    context = requireContext(),
                    onClick = {
                        settingsNavViewModel.goToUpgradeWithArgs {
                            setHighlightExtensionPosition(requireContext(), AddOns.getPinExtension(requireContext()))
                        }
                    }
                )
            }
            false
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

        /** Language code preference */ // TODO: Move this setting to Special Actions
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
            appSettings.setRenderMarkdownImage(newValue as Boolean)
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasKeyArgs<Args>()) {
            val args = getKeyArgs<Args>()
            if (args.highlightImproveDetection) highlightItemWithTitle(getString(R.string.adb_mode_title))
        }

        pinLockExtensionHelper.observePurchaseComplete().asLiveData().observe(viewLifecycleOwner) { unlock ->
            val preference = pinLockPreference ?: return@observe
            observeOnPreferenceInvalidate(preference) {
                val titleView = preference.titleView!!
                if (!unlock) {
                    AddOnsHelper.addPremiumIcon(titleView)
                } else {
                    AddOnsHelper.removePremiumIcon(titleView)
                }
            }
        }
    }

    override fun onStart() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            localBroadcastReceiver, IntentFilter(ACTION_CHECK_PREFERENCES)
        )
        improveDetectPreference?.isChecked = appSettings.isImproveDetectionEnabled()
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
        checkPreference?.isChecked = ClipboardAccessibilityService.isRunning(requireContext())
        if (rememberToCheckOverlaySwitch) {
            overlayPreference?.isChecked = isSystemOverlayEnabled(requireContext())
            rememberToCheckOverlaySwitch = false
            showSuggestion = true
        }
        if (rememberToCheckForPinLock || !pinLockExtensionHelper.isActive()) {
            pinLockPreference?.isChecked = PinLockHelper.isPinLockEnabled()
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

        private const val TEMP_CHECK_IMPROVE_ON_START = "temp_check_improve_on_start"
        private const val PIN_LOCK_PREF = "pin_lock_pref"
        private const val ACTIVE_ADB_MODE_PREF = "adb_mode_pref"
        private const val IMAGE_MARKDOWN_PREF = "image_markdown_pref"
        private const val SERVICE_PREF = "service_pref"

        fun checkImproveSettingsOnStart(context: Context, appSettings: AppSettings, preferenceProvider: PreferenceProvider) {
            val checkImprove = preferenceProvider.getBooleanKey(TEMP_CHECK_IMPROVE_ON_START, false)
            val canDetect = ClipboardLogDetector.isDetectionCompatible(context)
            if (checkImprove) {
                preferenceProvider.putBooleanKey(TEMP_CHECK_IMPROVE_ON_START, false)
                if (canDetect) {
                    appSettings.setImproveDetectionEnabled(canDetect)
                    preferenceProvider.putBooleanKey(ACTIVE_ADB_MODE_PREF, true)
                }
            }
        }

        fun checkForSettings(context: Context) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_CHECK_PREFERENCES))
        }
    }

    @Parcelize
    data class Args(val highlightImproveDetection: Boolean = false) : BaseArgs(), Parcelable
}