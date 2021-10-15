package com.kpstv.xclipper.extensions

import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import com.kpstv.common.R

fun CardView.setDefaultCardColor() {
    val backgroundColor = context.getColorAttr(android.R.attr.colorBackground, 0)
    if (ColorUtils.calculateLuminance(backgroundColor) < 0.5) {
        setCardBackgroundColor(context.colorFrom(R.color.cardview_dark_background))
    } else {
        setCardBackgroundColor(context.colorFrom(R.color.cardview_light_background))
    }
}