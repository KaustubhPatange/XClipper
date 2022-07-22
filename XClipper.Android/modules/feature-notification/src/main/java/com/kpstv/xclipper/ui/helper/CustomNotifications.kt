package com.kpstv.xclipper.ui.helper

import android.app.NotificationManager
import android.content.Context
import com.kpstv.xclipper.ui.helpers.CoreNotifications

object CustomNotifications {
    private const val CHANNEL_ID = CoreNotifications.CHANNEL_ID

    private val manager: NotificationManager get() = CoreNotifications.getNotificationManager()

    fun create(context: Context, text: String) : Unit = with(context) {
        CustomNotificationManager
    }
}