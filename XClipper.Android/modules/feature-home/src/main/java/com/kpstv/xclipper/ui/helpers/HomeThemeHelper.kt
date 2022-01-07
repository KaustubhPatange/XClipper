package com.kpstv.xclipper.ui.helpers

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.xclipper.feature_home.R

object HomeThemeHelper {
    // Add extra theme attributes specially for this context.
    fun apply(context: Context): Unit = with(context) {
        if (this is AppCompatActivity) {
            AppThemeHelper.applyDialogTheme(this)
        }
        if (AppThemeHelper.isLightVariant()) {
            theme.applyStyle(R.style.CommonThemeLight, true)
        } else {
            theme.applyStyle(R.style.CommonThemeDark, true)
        }
    }
}