package com.kpstv.xclipper.ui.fragments

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.kpstv.navigation.*
import com.kpstv.xclipper.di.SettingScreenHandler
import com.kpstv.xclipper.extensions.applyTopInsets
import com.kpstv.xclipper.extensions.drawableFrom
import com.kpstv.xclipper.ui.helpers.AppThemeHelper.registerForThemeChange
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_settings.databinding.FragmentSettingsBinding
import com.kpstv.xclipper.ui.fragments.settings.*
//import com.kpstv.xclipper.ui.activities.NavViewModel
//import com.kpstv.xclipper.ui.activities.Start
import com.kpstv.xclipper.ui.viewmodel.SettingNavViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.ui.navigation.AbstractNavigationOptionsExtensions.consume
import javax.inject.Inject

@AndroidEntryPoint
class Settings : ValueFragment(R.layout.fragment_settings), FragmentNavigator.Transmitter, LookFeelPreference.ThemeChangeCallbacks {
    private val binding by viewBinding(FragmentSettingsBinding::bind)
    private val viewModel by viewModels<SettingNavViewModel>()

    @Inject lateinit var settingScreenHandler: SettingScreenHandler

    private lateinit var navigator: FragmentNavigator

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerForThemeChange()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(binding.settingsContainer, Destination.of(SettingsFragment::class))

        setToolbar()
        viewModel.navigation.observe(viewLifecycleOwner, navigationObserver)

        if (hasKeyArgs<Args>()) {
            manageArguments()
        }

        childFragmentManager.addOnBackStackChangedListener call@{
            val current = navigator.getCurrentFragment() ?: return@call
            binding.toolbar.title = getString(settingScreenHandler.get(current::class).titleRes)
        }
    }

    private val navigationObserver = Observer { options: SettingNavViewModel.NavigationOptions? ->
        options?.consume {
            navigator.navigateTo(options.clazz, options.navOptions)
            binding.toolbar.title = getString(options.titleRes)
        }
    }

    private fun setToolbar() {
        binding.toolbar.applyTopInsets()
        binding.toolbar.navigationIcon = drawableFrom(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { goBack() }
    }

    private fun manageArguments() {
        val keys = getKeyArgs<Args>()
        if (keys.openLookFeel) {
            viewModel.navigateTo(settingScreenHandler.screenLookFeel(), animation = AnimationDefinition.None)
        }
    }

    override fun onThemeChanged(viewRect: Rect) {
        val navOptions = FragmentNavigator.NavOptions(
            args = Args(openLookFeel = true),
            animation = AnimationDefinition.CircularReveal(
                fromTarget = viewRect
            ),
            historyOptions = HistoryOptions.SingleTopInstance
        )
        parentNavigator.navigateTo(
            clazz = Settings::class,
            navOptions = navOptions
        )
    }

    @Parcelize
    data class Args(val openLookFeel: Boolean = false): BaseArgs(), Parcelable
}