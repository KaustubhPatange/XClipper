package com.kpstv.xclipper.extensions

import androidx.cardview.widget.CardView
import com.kpstv.core_extension.R

fun CardView.setDefaultCardColor() {
    val backgroundColor = context.getColorAttr(android.R.attr.colorBackground, 0)
    if (ColorExUtils.isDarkColor(backgroundColor)) {
        setCardBackgroundColor(context.colorFrom(R.color.cardview_dark_background))
    } else {
        setCardBackgroundColor(context.colorFrom(R.color.cardview_light_background))
    }
}