package com.kpstv.xclipper.extensions.utils

import android.content.Context
import com.kpstv.core.R
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.drawableFrom
import es.dmoral.toasty.Toasty

object ToastyUtils {
    fun showInfo(context: Context, message: String): Unit = with(context) {
        showToast(context, message, colorFrom(R.color.infoColor), colorFrom(R.color.colorBackground_Light))
    }
    fun showWarning(context: Context, message: String) : Unit = with(context) {
        showToast(context, message, colorFrom(R.color.warningColor), colorFrom(R.color.colorBackground))
    }

    private fun showToast(context: Context, message: String, color: Int, tintColor: Int) : Unit = with(context)  {
        Toasty.custom(
            context,
            message,
            drawableFrom(R.drawable.ic_logo_white)?.apply { setTint(tintColor) },
            color,
            tintColor,
            Toasty.LENGTH_SHORT,
            true,
            true
        ).show()
    }
}