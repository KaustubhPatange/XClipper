package com.kpstv.xclipper.ui.helpers

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kpstv.core.BuildConfig
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
    private const val LAUNCHER_ICON_RES_PREF = "launcher_icon_res_pref"

    // do not change order of the colors
    val baseColors = listOf(R.color.colorPrimary, R.color.magenta, R.color.purple, R.color.orange, R.color.orange_light, R.color.yellow, R.color.yellow_2, R.color.yellow_3, R.color.yellow_4, R.color.green, R.color.dark_green, R.color.blue, R.color.blue_2, R.color.blue_3, R.color.black, R.color.dark_grey)
    val baseColorAccentStyles = listOf(R.style.AppThemeOverlay_AccentDefault, R.style.AppThemeOverlay_AccentMagenta, R.style.AppThemeOverlay_AccentPurple, R.style.AppThemeOverlay_AccentOrange, R.style.AppThemeOverlay_AccentOrangeLight, R.style.AppThemeOverlay_AccentYellow, R.style.AppThemeOverlay_AccentYellow2, R.style.AppThemeOverlay_AccentYellow3, R.style.AppThemeOverlay_AccentYellow4, R.style.AppThemeOverlay_AccentGreen, R.style.AppThemeOverlay_AccentDarkGreen, R.style.AppThemeOverlay_AccentBlue, R.style.AppThemeOverlay_AccentBlue2, R.style.AppThemeOverlay_AccentBlue3, R.style.AppThemeOverlay_AccentBlack, R.style.AppThemeOverlay_AccentDarkGrey)
    val baseColorPrimaryStyles = listOf(R.style.AppThemeOverlay_AccentDefault, R.style.AppThemeOverlay_PrimaryMagenta, R.style.AppThemeOverlay_PrimaryPurple, R.style.AppThemeOverlay_PrimaryOrange, R.style.AppThemeOverlay_PrimaryOrangeLight, R.style.AppThemeOverlay_PrimaryYellow, R.style.AppThemeOverlay_PrimaryYellow2, R.style.AppThemeOverlay_PrimaryYellow3, R.style.AppThemeOverlay_PrimaryYellow4, R.style.AppThemeOverlay_PrimaryGreen, R.style.AppThemeOverlay_PrimaryDarkGreen, R.style.AppThemeOverlay_PrimaryBlue, R.style.AppThemeOverlay_PrimaryBlue2, R.style.AppThemeOverlay_PrimaryBlue3, R.style.AppThemeOverlay_PrimaryBlack, R.style.AppThemeOverlay_PrimaryDarkGrey)

    // do not change order of the icons
    val baseIcons = listOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher_magenta, R.mipmap.ic_launcher_purple, R.mipmap.ic_launcher_orange_light, R.mipmap.ic_launcher_yellow, R.mipmap.ic_launcher_green, R.mipmap.ic_launcher_blue, R.mipmap.ic_launcher_blue_3)
    private val baseIconsActivityAlias = listOf(".Default", ".Magenta", ".Purple", ".Orange_Light", ".Yellow", ".Green", ".Blue", ".Blue_3")

    @Volatile private var DARK_THEME = true
    @Volatile private var COLOR_PRIMARY_RES_ID_INDEX = 0
    @Volatile private var COLOR_ACCENT_RES_ID_INDEX = 0
    @Volatile private var LAUNCHER_ICON_RES_ID_INDEX = 0

    fun loadTheme(context: Context) {
        val pref = context.getSharedPreferences("theme", Context.MODE_PRIVATE)
        DARK_THEME = pref.getBoolean(IS_DARK_THEME, DARK_THEME)
        COLOR_PRIMARY_RES_ID_INDEX = pref.getInt(COLOR_PRIMARY_RES_ID_PREF, 0)
        COLOR_ACCENT_RES_ID_INDEX = pref.getInt(COLOR_ACCENT_RES_ID_PREF, 0)
        LAUNCHER_ICON_RES_ID_INDEX = pref.getInt(LAUNCHER_ICON_RES_PREF, 0)

        context.applyThemeColors()
    }

    fun setTheme(context: Context, style: AppTheme) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
            putBoolean(IS_DARK_THEME, style == AppTheme.DARK)
            DARK_THEME = style == AppTheme.DARK
        }
    }

    fun setColorPrimaryResIndex(context: Context, index: Int) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
            putInt(COLOR_PRIMARY_RES_ID_PREF, index)
            COLOR_PRIMARY_RES_ID_INDEX = index
        }
    }

    fun setColorAccentResIndex(context: Context, index: Int) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
            putInt(COLOR_ACCENT_RES_ID_PREF, index)
            COLOR_ACCENT_RES_ID_INDEX = index
        }
    }

    fun setLauncherIconFromResIndex(context: Context, index: Int) = with(context) {
        fun changeComponentSetting(aliasSuffix: String, enable: Boolean) {
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "${BuildConfig.APPLICATION_ID}$aliasSuffix"),
                if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
            putInt(LAUNCHER_ICON_RES_PREF, index)
            LAUNCHER_ICON_RES_ID_INDEX = index
        }
        // update the icon through activity aliases
        val currentAliasSuffix = baseIconsActivityAlias[index]
        changeComponentSetting(currentAliasSuffix, true)
        baseIconsActivityAlias.filterNot { it == currentAliasSuffix }.forEach { changeComponentSetting(it, false) }
    }

    fun resetColors(context: Context) {
        setColorPrimaryResIndex(context, 0)
        setColorAccentResIndex(context, 0)
    }

    fun resetLauncherIconRes(context: Context) {
        setLauncherIconFromResIndex(context, 0)
    }

    fun isDarkVariant() : Boolean = DARK_THEME
    fun isLightVariant() : Boolean = !DARK_THEME
    @ColorRes
    fun colorPrimaryRes() : Int = baseColors[COLOR_PRIMARY_RES_ID_INDEX]
    @ColorRes
    fun colorAccentRes() : Int = baseColors[COLOR_ACCENT_RES_ID_INDEX]
    fun launcherIconMipmapRes() : Int = baseIcons[LAUNCHER_ICON_RES_ID_INDEX]
    fun launcherIconDrawable(context: Context) : Drawable {
        val aliasSuffix = baseIconsActivityAlias[LAUNCHER_ICON_RES_ID_INDEX]
        return context.packageManager.getActivityIcon(
            ComponentName(BuildConfig.APPLICATION_ID, "${BuildConfig.APPLICATION_ID}$aliasSuffix")
        )
    }

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

        // apply colors to context & application context
        applyThemeColors()
        applicationContext.applyThemeColors()

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

    fun applyTheme(context: Context) = with(context) {
        if (isLightVariant()) {
            setTheme(R.style.AppTheme_Light)
        } else {
            setTheme(R.style.AppTheme_Dark)
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