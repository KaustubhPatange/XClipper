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
import com.kpstv.xclipper.extensions.SimpleFunction
import kotlinx.android.synthetic.main.fragment_welcome.view.*

class WelcomeUtils {
    companion object {

        fun setUpFragment(
            view: View,
            activity: FragmentActivity,
            @ColorRes paletteId: Int,
            @ColorRes nextPaletteId: Int,
            @StringRes textId: Int,
            @StringRes nextTextId: Int,
            action: NavDirections
        ) {
            setUpFragment(
                view = view,
                activity = activity,
                paletteId = paletteId,
                nextTextId = nextTextId,
                textId = textId,
                nextPaletteId = nextPaletteId,
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
            action: SimpleFunction
        ) {
            val palette = ContextCompat.getColor(activity, paletteId)
            val nextPalette = ContextCompat.getColor(activity, nextPaletteId)
            val white = ContextCompat.getColor(activity, android.R.color.white)

            val text = activity.getString(textId)

            activity.window.statusBarColor = palette
            with(view) {
                mainLayout.setBackgroundColor(palette)
                fw_textView.text =
                    SpannableString(text).apply {
                        setSpan(
                            ForegroundColorSpan(white),
                            0,
                            text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                if (insertView != null)
                    fw_insertLayout.addView(insertView)
                fw_btn_next.text = activity.getString(nextTextId)
                fw_btn_next.setTextColor(palette)
                fw_btn_next.backgroundTintList = ColorStateList.valueOf(nextPalette)
                fw_btn_next.setOnClickListener {
                    action.invoke()
                }
            }
        }
    }
}