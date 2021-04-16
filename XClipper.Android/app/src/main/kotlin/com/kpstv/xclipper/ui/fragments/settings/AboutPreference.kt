package com.kpstv.xclipper.ui.fragments.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.fragments.AnimatePreferenceFragment

class AboutPreference : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_pref, rootKey)

        findPreference<Preference>("author_pref")?.setOnPreferenceClickListener {
            Utils.commonUrlLaunch(requireContext(), getString(R.string.author_link))
            true
        }

        findPreference<Preference>("mail_pref")?.setOnPreferenceClickListener {
            Utils.commonUrlLaunch(requireContext(), "mailto:${getString(R.string.author_mail)}")
            true
        }

        findPreference<Preference>("package_pref")?.summary = requireContext().packageName
        findPreference<Preference>("version_pref")?.summary =
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        findPreference<Preference>("website_pref")?.apply {
            summary = getString(R.string.app_website)
            setOnPreferenceClickListener {
                Utils.commonUrlLaunch(requireContext(), getString(R.string.app_website))
                true
            }
        }

        findPreference<Preference>("report_pref")?.setOnPreferenceClickListener {
            Utils.commonUrlLaunch(requireContext(), getString(R.string.app_github_issues))
            true
        }
    }
}