package com.kpstv.xclipper.ui.fragments.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.xclipper.App.DARK_PREF
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.R

class LookFeelPreference(
    private val onThemeChange : (Boolean) -> Unit
) : PreferenceFragmentCompat(){

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.look_pref, rootKey)

        findPreference<SwitchPreferenceCompat>(DARK_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            DARK_THEME = newValue as Boolean
            onThemeChange.invoke(newValue)
            true
        }
    }
}