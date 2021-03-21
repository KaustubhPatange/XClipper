package com.kpstv.xclipper.extensions

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins

fun View.applyBottomInsets() {
    setOnApplyWindowInsetsListener { view, insets ->
        view.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            updateMargins(bottom = insets.systemWindowInsetBottom)
        }
        insets
    }
}