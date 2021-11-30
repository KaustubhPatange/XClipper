package com.kpstv.xclipper.ui.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.kpstv.core.R

object CoreNotifications {
    const val CHANNEL_ID = "my_channel_01"

    fun getNotificationManager(context: Context) : NotificationManager = with(context) {
        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun initialize(context: Context) = with(context) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }
}