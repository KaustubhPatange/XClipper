package com.kpstv.xclipper.extensions.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

object NotificationUtils {
    fun getPendingIntentFlags() = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
    fun getRandomCode(bounds: Int = 150) = Random().nextInt(400) + bounds

    fun getAppLaunchPendingIntent(context: Context) : PendingIntent = with(context) {
        val appLaunchIntent = packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return@with PendingIntent.getActivity(
            this,
            getRandomCode(bounds = 250),
            appLaunchIntent,
            getPendingIntentFlags()
        )
    }
}