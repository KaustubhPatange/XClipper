package com.kpstv.xclipper.extensions.utils

import android.app.PendingIntent
import android.os.Build
import java.util.*

object NotificationUtils {
    fun getPendingIntentFlags() = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
    fun getRandomCode(bounds: Int = 150) = Random().nextInt(400) + bounds
}