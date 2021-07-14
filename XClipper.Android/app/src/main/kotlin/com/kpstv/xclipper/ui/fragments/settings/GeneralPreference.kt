package com.kpstv.xclipper.ui.fragments.settings

 import android.content.BroadcastReceiver
 import android.content.Context
 import android.content.Intent
 import android.content.IntentFilter
 import android.os.Build
 import android.os.Bundle
 import androidx.localbroadcastmanager.content.LocalBroadcastManager
 import androidx.preference.*
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
 import com.kpstv.xclipper.extensions.Coroutines
 import com.kpstv.xclipper.extensions.utils.Utils.Companion.isClipboardAccessibilityServiceRunning
 import com.kpstv.xclipper.extensions.utils.Utils.Companion.isSystemOverlayEnabled
 import com.kpstv.xclipper.extensions.utils.Utils.Companion.retrievePackageList
 import com.kpstv.xclipper.extensions.utils.Utils.Companion.showAccessibilityDialog
 import com.kpstv.xclipper.extensions.utils.Utils.Companion.showDisableAccessibilityDialog
 import com.kpstv.xclipper.extensions.utils.Utils.Companion.showOverlayDialog
 import dagger.hilt.android.AndroidEntryPoint
 import es.dmoral.toasty.Toasty
 import javax.inject.Inject

@AndroidEntryPoint
class GeneralPreference : PreferenceFragmentCompat() {
    private val TAG = javaClass.simpleName
    private var checkPreference: SwitchPreferenceCompat? = null
    private var overlayPreference: SwitchPreferenceCompat? = null

    @Inject lateinit var preferenceProvider: PreferenceProvider

    /**
     * Since overlay permission makes you to leave the activity, the only way
     * to check the preference is to set a boolean and then in onResume() we
     * will set the preference.
     */
    private var rememberToCheckOverlaySwitch = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_pref, rootKey)

        /** Black list app preference */
        val blacklistPreference = findPreference<MultiSelectListPreference>(BLACKLIST_PREF)

        Coroutines.io {
            /** Load installed app list */
            App.appList = retrievePackageList(requireContext())
            blacklistPreference?.entries = App.appList.mapNotNull { it.label }.toTypedArray()
            blacklistPreference?.entryValues = App.appList.mapNotNull { it.packageName }.toTypedArray()
        }

        blacklistPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is Set<*>) {
                App.blackListedApps = newValue as Set<String>
            }
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

    companion object {
        const val ACTION_CHECK_PREFERENCES = "com.kpstv.xclipper.action_check_preferences"
        const val RESET_PREF = "reset_intro_pref"

        fun checkForSettings(context: Context) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_CHECK_PREFERENCES))
        }
    }
}