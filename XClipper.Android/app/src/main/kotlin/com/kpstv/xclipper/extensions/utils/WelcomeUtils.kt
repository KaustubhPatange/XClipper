package com.kpstv.xclipper.extensions.utils

import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.FragmentWelcomeBinding
import com.kpstv.xclipper.extensions.SimpleFunction

@Suppress("unused")
class WelcomeUtils {
    companion object {

        fun setUpFragment(
            view: View,
            activity: FragmentActivity,
            @ColorRes paletteId: Int,
            @ColorRes nextPaletteId: Int,
            @StringRes textId: Int,
            @StringRes nextTextId: Int,
            isLastScreen: Boolean = false,
            action: NavDirections
        ) {
            setUpFragment(
                view = view,
                activity = activity,
                paletteId = paletteId,
                nextTextId = nextTextId,
                textId = textId,
                nextPaletteId = nextPaletteId,
                isLastScreen = isLastScreen,
                action = {
                    view.findNavController().navigate(action)
                }
            )
        }

        fun setUpFragment(
            view: View,
            activity: FragmentActivity,
            @ColorRes paletteId: Int,
            @ColorRes nextPaletteId: Int,
            @StringRes textId: Int,
            @StringRes nextTextId: Int,
            insertView: View? = null,
            isLastScreen: Boolean = false,
            action: SimpleFunction
        ) {
            val palette = ContextCompat.getColor(activity, paletteId)
            val nextPalette = ContextCompat.getColor(activity, nextPaletteId)
            val white = ContextCompat.getColor(activity, android.R.color.white)

            val text = activity.getString(textId)

            activity.window.statusBarColor = palette
            activity.window.navigationBarColor = palette

            val binding = FragmentWelcomeBinding.bind(view)

            binding.root.setBackgroundColor(palette)
            binding.fwTextView.text =
                SpannableString(text).apply {
                    setSpan(
                        ForegroundColorSpan(white),
                        0,
                        text.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            if (insertView != null)
                binding.fwInsertLayout.addView(insertView)
            binding.fwBtnNext.text = activity.getString(nextTextId)
            binding.fwBtnNext.setTextColor(palette)
            binding.fwBtnNext.backgroundTintList = ColorStateList.valueOf(nextPalette)
            binding.fwBtnNext.setOnClickListener {
                // We are in the last screen of welcome fragment, we should remove the
                // status bar color overlay and keep it default to theme.
                if (isLastScreen) {
                    ThemeUtils.restoreStatusAndNavigationColor(activity)
                }
                action.invoke()
            }
        }
    }
}