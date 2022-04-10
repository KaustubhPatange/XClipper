package com.kpstv.xclipper.extensions.utils

import android.content.Context
import com.kpstv.core.R
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.drawableFrom
import es.dmoral.toasty.Toasty

object ToastyUtils {
    fun showInfo(context: Context, message: String): Unit = with(context) {
        showToast(context, message, colorFrom(R.color.colorCustomInfo))
    }
    fun showWarning(context: Context, message: String) : Unit = with(context) {
        showToast(context, message, colorFrom(R.color.warningColor))
    }

    private fun showToast(context: Context, message: String, color: Int) : Unit = with(context)  {
        val backgroundColor = colorFrom(R.color.colorBackground)
        Toasty.custom(
            context,
            message,
            drawableFrom(R.drawable.ic_logo_white)?.apply { setTint(backgroundColor) },
            color,
            backgroundColor,
            Toasty.LENGTH_SHORT,
            true,
            true
        ).show()
    }
}