package com.kpstv.xclipper.ui.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kpstv.core.R
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.utils.NotificationUtils

object CoreNotifications {
    const val CHANNEL_ID = "my_channel_01"

    private lateinit var manager: NotificationManager

    fun getNotificationManager(): NotificationManager = manager

    fun initialize(context: Context) = with(context) {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

    fun sendNotification(context: Context, title: String, message: String): Unit = with(context) {
        val randomCode = NotificationUtils.getRandomCode()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_white)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(NotificationUtils.getAppLaunchPendingIntent(this))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(getColorAttr(R.attr.colorAccent))
            .build()

        getNotificationManager().notify(randomCode, notification)
    }
}