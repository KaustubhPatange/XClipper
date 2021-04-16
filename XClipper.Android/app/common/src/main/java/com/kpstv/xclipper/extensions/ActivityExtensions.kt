package com.kpstv.xclipper.extensions

import android.app.Activity
import android.view.View

fun Activity.applyEdgeToEdgeMode() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
}