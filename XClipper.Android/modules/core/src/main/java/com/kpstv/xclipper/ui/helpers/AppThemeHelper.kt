package com.kpstv.xclipper.ui.helpers

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kpstv.core.R
import com.kpstv.xclipper.extensions.getColorAttr

enum class AppTheme(@StyleRes val style: Int) {
    DARK(R.style.AppTheme_Dark),
    LIGHT(R.style.AppTheme_Light)
}

object AppThemeHelper {

    private const val IS_DARK_THEME = "app_theme"
    private const val COLOR_PRIMARY_RES_ID_PREF = "color_primary_res_id_pref"
    private const val COLOR_ACCENT_RES_ID_PREF = "color_accent_res_id_pref"

    // do not change order of the colors
    val baseColors = listOf(R.color.colorPrimary, R.color.magenta, R.color.purple, R.color.orange, R.color.orange_light, R.color.yellow, R.color.yellow_2, R.color.yellow_3, R.color.yellow_4, R.color.green, R.color.blue, R.color.blue_2, R.color.blue_3)
    val baseColorAccentStyles = listOf(R.style.AppThemeOverlay_AccentDefault, R.style.AppThemeOverlay_AccentMagenta, R.style.AppThemeOverlay_AccentPurple, R.style.AppThemeOverlay_AccentOrange, R.style.AppThemeOverlay_AccentOrangeLight, R.style.AppThemeOverlay_AccentYellow, R.style.AppThemeOverlay_AccentYellow2, R.style.AppThemeOverlay_AccentYellow3, R.style.AppThemeOverlay_AccentYellow4, R.style.AppThemeOverlay_AccentGreen, R.style.AppThemeOverlay_AccentBlue, R.style.AppThemeOverlay_AccentBlue2, R.style.AppThemeOverlay_AccentBlue3)
    val baseColorPrimaryStyles = listOf(R.style.AppThemeOverlay_AccentDefault, R.style.AppThemeOverlay_PrimaryMagenta, R.style.AppThemeOverlay_PrimaryPurple, R.style.AppThemeOverlay_PrimaryOrange, R.style.AppThemeOverlay_PrimaryOrangeLight, R.style.AppThemeOverlay_PrimaryYellow, R.style.AppThemeOverlay_PrimaryYellow2, R.style.AppThemeOverlay_PrimaryYellow3, R.style.AppThemeOverlay_PrimaryYellow4, R.style.AppThemeOverlay_PrimaryGreen, R.style.AppThemeOverlay_PrimaryBlue, R.style.AppThemeOverlay_PrimaryBlue2, R.style.AppThemeOverlay_PrimaryBlue3)

    @Volatile private var DARK_THEME = true
    @Volatile private var COLOR_PRIMARY_RES_ID_INDEX = 0
    @Volatile private var COLOR_ACCENT_RES_ID_INDEX = 0

    fun loadTheme(context: Context) {
        val pref = context.getSharedPreferences("theme", Context.MODE_PRIVATE)
        DARK_THEME = pref.getBoolean(IS_DARK_THEME, DARK_THEME)
        COLOR_PRIMARY_RES_ID_INDEX = pref.getInt(COLOR_PRIMARY_RES_ID_PREF, 0)
        COLOR_ACCENT_RES_ID_INDEX = pref.getInt(COLOR_ACCENT_RES_ID_PREF, 0)
    }

    fun setTheme(context: Context, style: AppTheme) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
            putBoolean(IS_DARK_THEME, style == AppTheme.DARK)
            DARK_THEME = style == AppTheme.DARK
        }
    }

    fun setColorPrimaryResIndex(context: Context, @IntRange(from = 0, to = 2) index: Int) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
            putInt(COLOR_PRIMARY_RES_ID_PREF, index)
            COLOR_PRIMARY_RES_ID_INDEX = index
        }
    }

    fun setColorAccentResIndex(context: Context, @IntRange(from = 0, to = 3) index: Int) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
            putInt(COLOR_ACCENT_RES_ID_PREF, index)
            COLOR_ACCENT_RES_ID_INDEX = index
        }
    }

    fun isDarkVariant() : Boolean = DARK_THEME
    fun isLightVariant() : Boolean = !DARK_THEME
    @ColorRes
    fun colorPrimaryRes() : Int = baseColors.toList()[COLOR_PRIMARY_RES_ID_INDEX]
    @ColorRes
    fun colorAccentRes() : Int = baseColors.toList()[COLOR_ACCENT_RES_ID_INDEX]

    @Suppress("DEPRECATION")
    fun Context.updateTheme(activity: Activity) {
        val decorView = activity.window.decorView
        val style = if (isDarkVariant()) {
            setDarkColors(activity)
            AppTheme.DARK.style
        } else {
            setLightColors(activity)
            AppTheme.LIGHT.style
        }

        theme.applyStyle(style, true)

        // apply colors
        applyThemeColors()

        if (Build.VERSION.SDK_INT < 23) {
            activity.window.statusBarColor = Color.BLACK
            activity.window.navigationBarColor = Color.BLACK
        } else if (Build.VERSION.SDK_INT >= 23) {
            val color = activity.getColorAttr(R.attr.colorPrimary)
            activity.window.statusBarColor = color

            if (isLightVariant()) {
                decorView.systemUiVisibility =  decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }

        val color = activity.getColorAttr(R.attr.colorBackground)
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

    fun applyActivityTheme(activity: Activity) = with(activity) {
        if (isLightVariant()) {
            setLightColors(activity)
            setTheme(R.style.AppTheme_Light)
        } else {
            setDarkColors(activity)
        }
        applyThemeColors()
    }

    fun applyDialogTheme(activity: Activity) = with(activity) {
        if (isLightVariant()) {
            setLightColors(activity)
            setTheme(R.style.CustomDialogStyle_Light)
        } else {
            setTheme(R.style.CustomDialogStyle_Dark)
            setDarkColors(activity)
        }
        applyThemeColors()
    }

    private fun Context.applyThemeColors() {
        theme.applyStyle(baseColorPrimaryStyles[COLOR_PRIMARY_RES_ID_INDEX], true)
        theme.applyStyle(baseColorAccentStyles[COLOR_ACCENT_RES_ID_INDEX], true)
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