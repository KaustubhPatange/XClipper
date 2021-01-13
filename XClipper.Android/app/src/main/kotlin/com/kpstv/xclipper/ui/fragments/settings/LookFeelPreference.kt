package com.kpstv.xclipper.ui.fragments.settings

import android.os.Bundle
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.xclipper.App.DARK_PREF
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.R
import com.kpstv.xclipper.ui.fragments.AnimatePreferenceFragment

class LookFeelPreference(
    private val onThemeChange : (Boolean) -> Unit
) : AnimatePreferenceFragment(){

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.look_pref, rootKey)

        findPreference<SwitchPreferenceCompat>(DARK_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            DARK_THEME = newValue as Boolean
            onThemeChange.invoke(newValue)
            true
        }
    }
}