package com.kpstv.xclipper.ui.fragments.settings

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.globalVisibleRect
import com.kpstv.xclipper.ui.helpers.AppTheme
import com.kpstv.xclipper.ui.helpers.AppThemeHelper

class LookFeelPreference : PreferenceFragmentCompat(){
    interface ThemeChangeCallbacks {
        fun onThemeChanged(viewRect: Rect)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.look_pref, rootKey)

        findPreference<SwitchPreferenceCompat>(DARK_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            AppThemeHelper.setTheme(requireContext(), if (newValue as Boolean) AppTheme.DARK else AppTheme.LIGHT)
            val switchView = requireView().findViewById<View>(R.id.switchWidget)
            (parentFragment as ThemeChangeCallbacks).onThemeChanged(switchView.globalVisibleRect())
            true
        }
    }

    private companion object {
        const val DARK_PREF = "dark_pref"
    }
}