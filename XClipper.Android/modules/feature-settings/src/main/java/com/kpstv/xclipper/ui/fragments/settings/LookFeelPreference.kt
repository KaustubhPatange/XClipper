package com.kpstv.xclipper.ui.fragments.settings

import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.core.view.updateLayoutParams
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.ui.fragments.custom.AbstractPreferenceFragment
import com.kpstv.xclipper.ui.helpers.AppTheme
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import dagger.hilt.android.AndroidEntryPoint
import dev.sasikanth.colorsheet.ColorSheet

@AndroidEntryPoint
class LookFeelPreference : AbstractPreferenceFragment() {
    interface ThemeChangeCallbacks {
        fun onThemeChanged(viewRect: Rect? = null, animate: Boolean = true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.look_pref, rootKey)

        findPreference<SwitchPreferenceCompat>(DARK_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            AppThemeHelper.setTheme(requireContext(), if (newValue as Boolean) AppTheme.DARK else AppTheme.LIGHT)
            val switchView = requireView().findViewById<View>(R.id.switchWidget)
            (parentFragment as ThemeChangeCallbacks).onThemeChanged(switchView.globalVisibleRect())
            true
        }

        findPreference<Preference>(COLOR_ACCENT_PREF)?.setOnPreferenceClickListener {
            openForColorSelection(
                selectedColoRes = AppThemeHelper.colorAccentRes(),
                onChange = { AppThemeHelper.setColorAccentResIndex(requireContext(), it) }
            )
            true
        }

        findPreference<Preference>(COLOR_PRIMARY_PREF)?.setOnPreferenceClickListener {
            openForColorSelection(
                selectedColoRes = AppThemeHelper.colorPrimaryRes(),
                onChange = { AppThemeHelper.setColorPrimaryResIndex(requireContext(), it) }
            )
            true
        }
    }

    private fun openForColorSelection(selectedColoRes: Int, onChange: (index: Int) -> Unit) {
        val colors = AppThemeHelper.baseColors.map { colorFrom(it) }.toIntArray()
        val selectedColor = colorFrom(selectedColoRes)
        ColorSheet().let { sheet ->
            sheet.colorPicker(
                colors = colors,
                selectedColor = selectedColor,
                noColorOption = false,
                listener = { color ->
                    sheet.dismiss()
                    onChange(colors.indexOf(color))
                    (parentFragment as ThemeChangeCallbacks).onThemeChanged(animate = false)
                }
            )
            sheet.show(childFragmentManager)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeColorPreference(prefName = COLOR_PRIMARY_PREF, attr = R.attr.colorPrimary)
        observeColorPreference(prefName = COLOR_ACCENT_PREF, attr = R.attr.colorAccent)
    }

    private fun observeColorPreference(prefName: String, @AttrRes attr: Int) {
        val colorAccentPref = findPreference<Preference>(prefName)!!
        observeOnPreferenceInvalidate(colorAccentPref) call@{
            val imageView = colorAccentPref.imageView ?: return@call
            imageView.updateLayoutParams<ViewGroup.LayoutParams> {
                val dp24 = requireContext().toPx(24).toInt()
                width = dp24
                height = dp24
            }
            imageView.background = ColorDrawable(requireContext().getColorAttr(attr))
        }
    }

    private companion object {
        private const val COLOR_ACCENT_PREF = "color_accent_pref"
        private const val COLOR_PRIMARY_PREF = "color_primary_pref"
        const val DARK_PREF = "dark_pref"
    }
}