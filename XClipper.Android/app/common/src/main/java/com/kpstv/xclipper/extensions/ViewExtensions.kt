package com.kpstv.xclipper.extensions

import android.graphics.Rect
import android.view.View

fun View.globalVisibleRect(): Rect {
    val rect = Rect()
    getGlobalVisibleRect(rect)
    return rect
}