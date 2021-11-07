package com.kpstv.xclipper.extensions

import android.view.View

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.collapse() {
    this.visibility = View.GONE
}
