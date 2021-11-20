package com.kpstv.xclipper.extensions.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrateUtils {
    @Suppress("DEPRECATION")
    fun vibrateDevice(context: Context) {
        val m = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            m.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            m.vibrate(50)
        }
    }
}