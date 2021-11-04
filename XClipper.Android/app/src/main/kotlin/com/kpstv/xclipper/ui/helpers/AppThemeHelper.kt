package com.kpstv.xclipper.ui.helpers

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils

enum class AppTheme(@StyleRes val style: Int) {
    DARK(R.style.AppTheme_Dark),
    LIGHT(R.style.AppTheme_Light)
}

object AppThemeHelper {

    private const val IS_DARK_THEME = "app_theme"

    @Volatile
    private var DARK_THEME = true

    fun loadTheme(context: Context) {
        DARK_THEME = context.getSharedPreferences("theme", Context.MODE_PRIVATE).getBoolean(
            IS_DARK_THEME, DARK_THEME
        )
    }

    fun setTheme(context: Context, style: AppTheme) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
            putBoolean(IS_DARK_THEME, style == AppTheme.DARK)
            DARK_THEME = style == AppTheme.DARK
        }
    }

    fun isDarkVariant() : Boolean = DARK_THEME
    fun isLightVariant() : Boolean = !DARK_THEME

    @Suppress("DEPRECATION")
    fun Context.updateTheme(activity: FragmentActivity) {
        val decorView = activity.window.decorView
        val style = if (isDarkVariant()) {
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

            if (isLightVariant()) {
                decorView.systemUiVisibility =  decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
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

    fun applyActivityTheme(activity: AppCompatActivity) {
        if (isLightVariant()) {
            setLightColors(activity)
            activity.setTheme(R.style.AppTheme_Light)
        } else {
            setDarkColors(activity)
        }
    }

    fun applyDialogTheme(activity: Activity) {
        if (isLightVariant()) {
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