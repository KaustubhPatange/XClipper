package com.kpstv.xclipper.extensions.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R

class ThemeUtils {
    enum class AppTheme(@StyleRes val style: Int) {
        DARK(R.style.AppTheme_Dark),
        LIGHT(R.style.AppTheme_Light)
    }
    companion object {
        fun Context.updateTheme(activity: FragmentActivity) {
            val decorView = activity.window.decorView
            val style = if (App.DARK_THEME) {
                setDarkColors(activity)
                AppTheme.DARK.style
            } else {
                setLightColors(activity)
                AppTheme.LIGHT.style
            }

            theme.applyStyle(style, true)

            if (Build.VERSION.SDK_INT < 23) {
                activity.window.statusBarColor = Color.BLACK
                activity.window.navigationBarColor = Color.BLACK
            } else if (Build.VERSION.SDK_INT >= 23) {
                val color = Utils.getDataFromAttr(activity, R.attr.colorPrimary)
                activity.window.statusBarColor = color

                if (!App.DARK_THEME) {
                    decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }

            val color = Utils.getDataFromAttr(activity, R.attr.colorBackground)
            activity.window.setBackgroundDrawable(ColorDrawable(color))
        }

        /**
         * Once the theme is changed the fragment will be respond to the theme changes.
         */
        fun Fragment.registerForThemeChange() {
            this.lifecycle.addObserver(object: DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    context?.updateTheme(requireActivity())
                    lifecycle.removeObserver(this)
                    super.onCreate(owner)
                }
            })
        }

        var CARD_COLOR: Int = 0
        var CARD_CLICK_COLOR: Int = 0
        var CARD_SELECTED_COLOR : Int = 0

        fun setTheme(activity: AppCompatActivity) {
            if (!App.DARK_THEME) {
                setLightColors(activity)
                activity.setTheme(R.style.AppTheme_Light)
            } else {
                setDarkColors(activity)
            }
        }

        fun setDialogTheme(activity: Activity) {
            if (!App.DARK_THEME) {
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