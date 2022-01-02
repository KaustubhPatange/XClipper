package com.kpstv.xclipper.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

@Suppress("DEPRECATION")
fun View.applyBottomInsets(to: View = this, merge: Boolean = false, pad: Boolean = false, extra: Int = 0) {
    val marginBottom = to.marginBottom
    val paddingBottom = to.paddingBottom
    setOnApplyWindowInsetsListener { v, insets ->
        if (pad) {
            v.updatePadding(bottom = insets.systemWindowInsetBottom + extra + if (merge) paddingBottom else 0)
        } else {
            to.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(bottom = insets.systemWindowInsetBottom + extra + if (merge) marginBottom else 0)
            }
        }
        insets
    }
}

@Suppress("DEPRECATION")
fun View.applyTopInsets(to: View = this, merge: Boolean = false, pad: Boolean = false, extra: Int = 0) {
    val marginTop = to.marginTop
    val paddingTop = to.paddingTop
    setOnApplyWindowInsetsListener { v, insets ->
        if (pad) {
            v.updatePadding(top = insets.systemWindowInsetTop + extra + if (merge) paddingTop else 0)
        } else {
            to.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(top = insets.systemWindowInsetTop + extra + if (merge) marginTop else 0)
            }
        }
        insets
    }
}