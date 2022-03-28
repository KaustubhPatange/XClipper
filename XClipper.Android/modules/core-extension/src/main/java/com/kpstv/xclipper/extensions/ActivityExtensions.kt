package com.kpstv.xclipper.extensions

import android.app.Activity
import android.graphics.Rect
import android.view.View

@Suppress("DEPRECATION")
fun Activity.applyEdgeToEdgeMode() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
}

val Activity.statusBarHeight : Int
    get() {
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect.top
    }