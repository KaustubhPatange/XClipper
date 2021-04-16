package com.kpstv.xclipper.ui.fragments.settings

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.xclipper.App.DARK_PREF
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.globalVisibleRect
import com.kpstv.xclipper.ui.fragments.AnimatePreferenceFragment

class LookFeelPreference : PreferenceFragmentCompat(){
    interface ThemeChangeCallbacks {
        fun onThemeChanged(viewRect: Rect)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.look_pref, rootKey)

        findPreference<SwitchPreferenceCompat>(DARK_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            DARK_THEME = newValue as Boolean
            val switchView = requireView().findViewById<View>(R.id.switchWidget)
            (parentFragment as ThemeChangeCallbacks).onThemeChanged(switchView.globalVisibleRect())
            true
        }
    }
}