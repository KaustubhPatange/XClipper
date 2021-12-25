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
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.getKeyArgs
import com.kpstv.navigation.hasKeyArgs
import com.kpstv.xclipper.PinLockHelper
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.DialogProgressBinding
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.extensions.helper.ClipboardLogDetector
import com.kpstv.xclipper.extensions.utils.PackageUtils
import com.kpstv.xclipper.extensions.utils.SystemUtils.isSystemOverlayEnabled
import com.kpstv.xclipper.ui.CoreDialogs
import com.kpstv.xclipper.ui.dialogs.ClipboardServiceDialogs
import com.kpstv.xclipper.ui.dialogs.Dialogs
import com.kpstv.xclipper.ui.dialogs.MultiSelectDialogBuilder
import com.kpstv.xclipper.ui.dialogs.MultiSelectModel3
import com.kpstv.xclipper.ui.fragments.custom.AbstractPreferenceFragment
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
                CoreDialogs.showSystemOverlayDialog(requireContext())

                if (newValue == true) rememberToCheckOverlaySwitch = true

                return@setOnPreferenceChangeListener false
            }
            appSettings.setShowClipboardSuggestions(newValue as Boolean)
            true
        }

        /** Clipboard Service preference */
        checkPreference = findPreference(SERVICE_PREF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) // Add that BETA tag to summary for Android 10+ users
            checkPreference?.summary = "[BETA] ${checkPreference?.summary}"
        checkPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                ClipboardServiceDialogs.showAccessibilityDialog(requireContext()) { checkForService() }
            } else {
                ClipboardServiceDialogs.showDisableAccessibilityDialog(requireContext()) { checkForService() }
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
            appSettings.setSwipeDeleteEnabledForClipItem(newValue as Boolean)
            true
        }

        /** Text trimming while displaying */
        findPreference<SwitchPreferenceCompat>(TRIM_CLIP_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            appSettings.setTextTrimmingEnabled(newValue as Boolean)
            true
        }

        /** Reset onboarding screens **/
        findPreference<Preference>(RESET_PREF)?.setOnPreferenceClickListener {
            appSettings.setBubbleOnBoardingDialogShown(false)
            appSettings.setOnBoardingScreensShowed(false)
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
            appSettings.setShowClipboardSuggestions(true)
        }
        if (rememberToCheckForPinLock || !pinLockExtensionHelper.isActive()) {
            pinLockPreference?.isChecked = PinLockHelper.isPinLockEnabled()
        }
    }

    private fun showBlacklistAppDialog() {
        val job = SupervisorJob()
        CoroutineScope(Dispatchers.IO + job).launch {
            val apps = PackageUtils.retrievePackageList(requireContext())
            val currentBlackListApps = appSettings.getClipboardMonitoringBlackListApps()
            lifecycleScope.launch {
                appsDialog?.dismiss()
                appsDialog = MultiSelectDialogBuilder(
                    context = requireContext(),
                    itemsCheckedState = { itemsCheckedState ->
                        val packages = itemsCheckedState.filter { it.value }.map { apps[it.key].packageName?.toString() }
                            .filterNotNull().toSet()
                        appSettings.setClipboardMonitoringBlackListApps(packages)
                    }
                ).apply {
                    setTitle(getString(R.string.blacklist_apps))
                    setItems(apps.map { pkg ->
                        MultiSelectModel3(
                            title = pkg.label.toString(),
                            subtitle = pkg.packageName.toString(),
                            isChecked = currentBlackListApps.contains(pkg.packageName)
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

        private const val RESET_PREF = "reset_intro_pref"
        private const val TEMP_CHECK_IMPROVE_ON_START = "temp_check_improve_on_start"
        private const val PIN_LOCK_PREF = "pin_lock_pref"
        private const val ACTIVE_ADB_MODE_PREF = "adb_mode_pref"
        private const val IMAGE_MARKDOWN_PREF = "image_markdown_pref"
        private const val SERVICE_PREF = "service_pref"
        private const val SUGGESTION_PREF = "suggestion_pref"
        private const val SWIPE_DELETE_PREF = "swipe_delete_pref"
        private const val TRIM_CLIP_PREF = "trim_clip_pref"
        private const val BLACKLIST_PREF = "blacklist_pref"

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

        fun refreshSettings(context: Context) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_CHECK_PREFERENCES))
        }
    }

    @Parcelize
    data class Args(val highlightImproveDetection: Boolean = false) : BaseArgs(), Parcelable
}