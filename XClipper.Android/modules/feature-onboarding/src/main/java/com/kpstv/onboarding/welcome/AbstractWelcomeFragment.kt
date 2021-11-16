package com.kpstv.onboarding.welcome

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.HistoryOptions
import com.kpstv.navigation.NavAnimation
import com.kpstv.navigation.ValueFragment
import com.kpstv.onboarding.internals.OnBoardingNavViewModel
import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.welcome.R
import com.kpstv.welcome.databinding.FragmentWelcomeBinding
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.applyBottomInsets
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.helpers.AppSettings
import javax.inject.Inject

internal abstract class AbstractWelcomeFragment : ValueFragment(R.layout.fragment_welcome) {

    protected data class Configuration(
        @ColorRes val paletteId: Int,
        @ColorRes val nextPaletteId: Int,
        @StringRes val nextTextId: Int,
        @StringRes val textId: Int,
        /**
         * Set [textId] to -1 to use this
         */
        val text: Spanned = SpannableString(""),
        val insertView: View? = null,
        val isLastScreen: Boolean = false,
        val action: SimpleFunction? = null,
        val directions: OnBoardingRoutes? = null
    )

    companion object {
        private var previousPaletteColor: Int = 0
    }

    private val binding by viewBinding(FragmentWelcomeBinding::bind)
    private val navViewModel by viewModels<OnBoardingNavViewModel>(ownerProducer = ::requireParentFragment)

    @Inject
    lateinit var appSettings: AppSettings

    protected abstract fun getConfigurations() : Configuration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val configs = getConfigurations()

        val palette = ContextCompat.getColor(requireContext(), configs.paletteId)
        val nextPalette = ContextCompat.getColor(requireContext(), configs.nextPaletteId)

        animateLayoutColors(palette)

        val spannedText : Spanned = if (configs.textId != -1) {
            SpannableStringBuilder(getString(configs.textId))
        } else {
            configs.text
        }
        binding.fwTextView.text = spannedText
        if (configs.insertView != null)
            binding.fwInsertLayout.addView(configs.insertView)

        binding.fwBtnNext.applyBottomInsets()
        binding.fwBtnNext.text = getString(configs.nextTextId)
        binding.fwBtnNext.setTextColor(palette)
        binding.fwBtnNext.backgroundTintList = ColorStateList.valueOf(nextPalette)
        binding.fwBtnNext.setOnClickListener {
            // This will be used to create a color transition.
            previousPaletteColor = palette

            if (configs.isLastScreen) {
                appSettings.setOnBoardingScreensShowed(true)
            }

            if (configs.directions != null)
                navigateTo(configs.directions)
            else if (configs.action != null)
                configs.action.invoke()
        }
    }

    fun navigateTo(screen: OnBoardingRoutes, popUpTo: Boolean = false, animation: NavAnimation? = null) {
        val anim = animation ?: AnimationDefinition.Shared(
            mapOf(
                binding.fwTextView to "paragraphText",
                binding.fwInsertLayout to "insertLayout"
            )
        )
        navViewModel.navigateTo(
            screen = screen,
            remember = !popUpTo,
            animation = anim,
            historyOptions = if (popUpTo) HistoryOptions.ClearHistory else HistoryOptions.None
        )
    }

    private fun animateLayoutColors(currentPaletteColor: Int) {
        if (previousPaletteColor != 0) {
            ValueAnimator.ofArgb(previousPaletteColor, currentPaletteColor).apply {
                addUpdateListener {
                    // If user is quickly navigating back or through next button
                    // Fragment's view becomes null if it's removed or being created.
                    if (!isRemoving && isAdded)
                        binding.root.setBackgroundColor(it.animatedValue as Int)
                }
                start()
            }
        } else {
            binding.root.setBackgroundColor(currentPaletteColor)
        }
    }
}