package com.kpstv.xclipper.ui.fragments.welcome

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.NavAnimation
import com.kpstv.navigation.ValueFragment
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.FragmentWelcomeBinding
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.applyBottomInsets
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.activities.NavViewModel
import com.kpstv.xclipper.ui.activities.Start

abstract class AbstractWelcomeFragment : ValueFragment(R.layout.fragment_welcome) {

    protected data class Configuration(
        @ColorRes val paletteId: Int,
        @ColorRes val nextPaletteId: Int,
        @StringRes val textId: Int,
        @StringRes val nextTextId: Int,
        val insertView: View? = null,
        val isLastScreen: Boolean = false,
        val action: SimpleFunction? = null,
        val directions: Start.Screen? = null
    )

    companion object {
        private var previousPaletteColor: Int = 0
    }

    private val binding by viewBinding(FragmentWelcomeBinding::bind)
    private val navViewModel by activityViewModels<NavViewModel>()

    protected abstract fun getConfigurations() : Configuration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val configs = getConfigurations()

        val palette = ContextCompat.getColor(requireContext(), configs.paletteId)
        val nextPalette = ContextCompat.getColor(requireContext(), configs.nextPaletteId)
        val white = ContextCompat.getColor(requireContext(), android.R.color.white)

        val text = getString(configs.textId)

        animateLayoutColors(palette)

        binding.fwTextView.text =
            SpannableString(text).apply {
                setSpan(
                    ForegroundColorSpan(white),
                    0,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        if (configs.insertView != null)
            binding.fwInsertLayout.addView(configs.insertView)

        binding.fwBtnNext.applyBottomInsets()
        binding.fwBtnNext.text = getString(configs.nextTextId)
        binding.fwBtnNext.setTextColor(palette)
        binding.fwBtnNext.backgroundTintList = ColorStateList.valueOf(nextPalette)
        binding.fwBtnNext.setOnClickListener {
            // This will be used to create a color transition.
            previousPaletteColor = palette

            // We are in the last screen of welcome fragment, we should remove the
            // status bar color overlay and keep it default to theme.
            if (configs.isLastScreen) { // TODO:
               /* requireActivity().window.decorView.systemUiVisibility = 0
                ThemeUtils.restoreStatusAndNavigationColor(requireActivity())*/
            }

            if (configs.directions != null)
                navigateTo(configs.directions)
            else if (configs.action != null)
                configs.action.invoke()
        }
    }

    fun navigateTo(screen: Start.Screen, popUpTo: Boolean = false, animation: NavAnimation? = null) {
        val anim = animation ?: AnimationDefinition.Shared(
            mapOf(
                binding.fwTextView to "paragraphText",
                binding.fwInsertLayout to "insertLayout"
            )
        )
        navViewModel.navigateTo(
            screen = screen,
            addToBackStack = !popUpTo,
            animation = anim,
            popUpTo  = popUpTo
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