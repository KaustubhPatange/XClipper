package com.kpstv.xclipper.di.feature_settings.improve_detection

import android.view.ViewGroup
import com.kpstv.xclipper.di.improve_detection.ImproveDetectionQuickTip
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.ui.helpers.fragments.ImproveDetectionHelper
import javax.inject.Inject

class ImproveDetectionQuickTipImpl @Inject constructor() : ImproveDetectionQuickTip {
    override fun add(container: ViewGroup, doOnAction: SimpleFunction) {
        ImproveDetectionHelper.addQuickTip(container, doOnAction)
    }
}