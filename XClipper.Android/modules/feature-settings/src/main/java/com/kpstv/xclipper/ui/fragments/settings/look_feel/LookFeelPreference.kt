package com.kpstv.xclipper.ui.fragments.settings.look_feel

import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.di.SettingScreenHandler
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.ui.fragments.custom.AbstractPreferenceFragment
import com.kpstv.xclipper.ui.helpers.AddOnsHelper
import com.kpstv.xclipper.ui.helpers.AppTheme
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import com.kpstv.xclipper.ui.sheet.LauncherIconSelectionSheet
import com.kpstv.xclipper.ui.viewmodel.SettingNavViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.sasikanth.colorsheet.ColorSheet
import javax.inject.Inject

@AndroidEntryPoint
class LookFeelPreference : AbstractPreferenceFragment(), LauncherIconSelectionSheet.Callback {
    interface ThemeChangeCallbacks {
        fun onThemeChanged(viewRect: Rect? = null, animate: Boolean = true)
    }

    @Inject lateinit var settingScreenHandler: SettingScreenHandler

    private val settingNavViewModel by viewModels<SettingNavViewModel>(ownerProducer = ::requireParentFragment)

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

        findPreference<Preference>(LAUNCHER_ICON_PREF)?.setOnPreferenceClickListener {
            LauncherIconSelectionSheet.show(
                fragmentManager = childFragmentManager
            )
            true
        }

        findPreference<CustomizeQuickTipPreference>(CUSTOMIZE_TIP_PREF)?.inject(
            settingScreenHandler = settingScreenHandler,
            settingNavViewModel = settingNavViewModel
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val themeExtensionHelper = AddOnsHelper.getHelperForCustomizeTheme(requireContext())

        observeColorPreference(prefName = COLOR_PRIMARY_PREF, attr = R.attr.colorPrimary)
        observeColorPreference(prefName = COLOR_ACCENT_PREF, attr = R.attr.colorAccent)

        // for launcher icon
        observeOnPreferenceInvalidate(findPreference(LAUNCHER_ICON_PREF)!!) call@{
            val imageView = imageView ?: return@call
            imageView.updateLayoutParams<ViewGroup.LayoutParams> {
                val dp24 = requireContext().toPx(32).toInt()
                width = dp24
                height = dp24
            }
            imageView.setImageDrawable(AppThemeHelper.launcherIconDrawable(requireContext()))
        }

        // custom logic for hiding or showing premium tip
        themeExtensionHelper.observePurchaseComplete().asLiveData().observe(viewLifecycleOwner) { unlock ->
            findPreference<Preference>(COLOR_PRIMARY_PREF)?.isEnabled = unlock
            findPreference<Preference>(COLOR_ACCENT_PREF)?.isEnabled = unlock
            findPreference<Preference>(LAUNCHER_ICON_PREF)?.isEnabled = unlock
            findPreference<Preference>(CUSTOMIZE_TIP_PREF)?.isVisible = !unlock
        }
    }

    override fun onIconSelected(index: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(drawableFrom(AppThemeHelper.baseIcons[index]))
            .setTitle(R.string.dialog_icon_title)
            .setMessage(R.string.dialog_icon_message)
            .setPositiveButton(getString(R.string.alright)) { _, _ ->
                AppThemeHelper.setLauncherIconFromResIndex(requireContext(), index)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
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

    private fun observeColorPreference(prefName: String, @AttrRes attr: Int) {
        observeOnPreferenceInvalidate(findPreference(prefName)!!) call@{
            val imageView = imageView ?: return@call
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
        private const val LAUNCHER_ICON_PREF = "launcher_icon_pref"
        private const val CUSTOMIZE_TIP_PREF = "customize_tip_pref"
        const val DARK_PREF = "dark_pref"
    }
}