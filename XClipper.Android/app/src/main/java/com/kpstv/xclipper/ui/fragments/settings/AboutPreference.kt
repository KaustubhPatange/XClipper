package com.kpstv.xclipper.ui.fragments.settings

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.R

class AboutPreference : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_pref, rootKey)

        findPreference<Preference>("author_pref")?.setOnPreferenceClickListener {
            commonIntentLaunch(getString(R.string.author_link))
            true
        }

        findPreference<Preference>("mail_pref")?.setOnPreferenceClickListener {
            commonIntentLaunch("mailto:${getString(R.string.author_mail)}")
            true
        }

        findPreference<Preference>("package_pref")?.summary = requireContext().packageName
        findPreference<Preference>("version_pref")?.summary =
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        findPreference<Preference>("website_pref")?.apply {
            summary = getString(R.string.app_website)
            setOnPreferenceClickListener {
                commonIntentLaunch(getString(R.string.app_website))
                true
            }
        }

        findPreference<Preference>("report_pref")?.setOnPreferenceClickListener {
            commonIntentLaunch(getString(R.string.app_github_issues))
            true
        }
    }

    private fun commonIntentLaunch(value: String) {
        val intent = Intent(ACTION_VIEW).apply {
            data = Uri.parse(value)
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}