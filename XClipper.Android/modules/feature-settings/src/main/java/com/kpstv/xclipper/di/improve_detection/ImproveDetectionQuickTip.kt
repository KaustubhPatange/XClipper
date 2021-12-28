package com.kpstv.xclipper.di.improve_detection

import android.view.ViewGroup
import com.kpstv.xclipper.extensions.SimpleFunction

interface ImproveDetectionQuickTip {
    fun add(container: ViewGroup, doOnAction: SimpleFunction)
}