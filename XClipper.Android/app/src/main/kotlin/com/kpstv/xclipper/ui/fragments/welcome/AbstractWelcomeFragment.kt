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
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.FragmentWelcomeBinding
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.applyBottomInsets
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.extensions.viewBinding

abstract class AbstractWelcomeFragment : Fragment(R.layout.fragment_welcome) {

    protected data class Configuration(
        @ColorRes val paletteId: Int,
        @ColorRes val nextPaletteId: Int,
        @StringRes val textId: Int,
        @StringRes val nextTextId: Int,
        val insertView: View? = null,
        val isLastScreen: Boolean = false,
        val action: SimpleFunction? = null,
        val directions: NavDirections? = null
    )

    companion object {
        private var previousPaletteColor: Int = 0
    }

    private val binding by viewBinding(FragmentWelcomeBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        requireActivity().window.statusBarColor = 0
        requireActivity().window.navigationBarColor = 0
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

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
            if (configs.isLastScreen) {
                requireActivity().window.decorView.systemUiVisibility = 0
                ThemeUtils.restoreStatusAndNavigationColor(requireActivity())
            }

            if (configs.directions != null)
                navigateTo(configs.directions)
            else if (configs.action != null)
                configs.action.invoke()
        }
    }

    fun navigateTo(direction: NavDirections, options: NavOptions? = null) {
        val extras = FragmentNavigatorExtras(
            binding.fwTextView to "paragraphText",
            binding.fwInsertLayout to "insertLayout"
        )

        findNavController().navigate(direction.actionId, null, options, extras)
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