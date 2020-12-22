package com.kpstv.xclipper.extensions.utils

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.R

class ThemeUtils {
    companion object {
        var CARD_COLOR: Int = 0
        var CARD_CLICK_COLOR: Int = 0
        var CARD_SELECTED_COLOR : Int = 0

        fun setTheme(activity: AppCompatActivity) {
            if (!DARK_THEME) {
                setLightColors(activity)
                activity.setTheme(R.style.AppTheme_Light)
            } else {
                setDarkColors(activity)
            }
        }

        fun setDialogTheme(activity: Activity) {
            if (!DARK_THEME) {
                setLightColors(activity)
                activity.setTheme(R.style.CustomDialogStyle_Light)
            } else {
                setDarkColors(activity)
            }
        }

        private fun setDarkColors(activity: Activity) = with(activity) {
            CARD_COLOR = ContextCompat.getColor(this, R.color.colorCard)
            CARD_CLICK_COLOR = ContextCompat.getColor(this, R.color.colorClickCard)
            CARD_SELECTED_COLOR = ContextCompat.getColor(this, R.color.colorSelected)
        }

        private fun setLightColors(activity: Activity) = with(activity) {
            CARD_COLOR = ContextCompat.getColor(this, R.color.colorCard_Light)
            CARD_CLICK_COLOR = ContextCompat.getColor(this, R.color.colorClickCard_Light)
            CARD_SELECTED_COLOR = ContextCompat.getColor(this, R.color.colorSelected_Light)
        }
    }
}