package com.kpstv.xclipper.extensions.utils

import android.content.Context
import com.kpstv.core.R
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.drawableFrom
import es.dmoral.toasty.Toasty

object ToastyUtils {
    fun showWarning(context: Context, message: String) : Unit = with(context) {
        val backgroundColor = colorFrom(R.color.colorBackground)
        Toasty.custom(
            context,
            message,
            drawableFrom(R.drawable.ic_logo_white)?.apply { setTint(backgroundColor) },
            colorFrom(R.color.warningColor),
            backgroundColor,
            Toasty.LENGTH_SHORT,
            true,
            true
        ).show()
    }
}