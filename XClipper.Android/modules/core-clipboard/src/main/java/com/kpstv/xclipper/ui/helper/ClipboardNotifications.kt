package com.kpstv.xclipper.ui.helper

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.core_clipboard.R
import com.kpstv.xclipper.extensions.utils.NotificationUtils
import com.kpstv.xclipper.service.receiver.ClipboardBroadcastReceiver
import com.kpstv.xclipper.ui.helpers.CoreNotifications
import com.kpstv.xclipper.ui.helpers.CoreNotifications.getNotificationManager

object ClipboardNotifications {
    private const val CHANNEL_ID = CoreNotifications.CHANNEL_ID

    private const val ACCESSIBILITY_NOTIFICATION_ID = 34

    fun sendAccessibilityDisabledNotification(context: Context): Unit = with(context) {
        val openIntent = PendingIntent.getBroadcast(
            this,
            NotificationUtils.getRandomCode(),
            ClipboardBroadcastReceiver.createAccessibilityServiceOpenAction(this),
            NotificationUtils.getPendingIntentFlags()
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_white)
            .setContentTitle(getString(R.string.clipboard_disabled_text))
            .setContentText(getString(R.string.clipboard_disabled_content))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentIntent(openIntent)
            .build()

        getNotificationManager().notify(ACCESSIBILITY_NOTIFICATION_ID, notification)
    }
}