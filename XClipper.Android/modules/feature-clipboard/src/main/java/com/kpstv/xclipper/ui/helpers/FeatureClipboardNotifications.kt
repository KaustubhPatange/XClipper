package com.kpstv.xclipper.ui.helpers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.utils.NotificationUtils
import com.kpstv.xclipper.feature_clipboard.R
import com.kpstv.xclipper.service.receiver.ImproveDetectionReceiver

object FeatureClipboardNotifications {

    private const val CHANNEL_ID = CoreNotifications.CHANNEL_ID

    private const val IMPROVE_DETECTION_DISABLED_NOTIFICATION_ID = 35

    private val manager: NotificationManager get() = CoreNotifications.getNotificationManager()

    fun sendImproveDetectionDisabled(context: Context) = with(context) {
        val learnMoreIntent = ImproveDetectionReceiver.createOpenUrlAction(this, getString(R.string.app_docs_improve_detect))

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.improve_detection_notify_title))
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.improve_detection_notify_text)))
            .setSmallIcon(R.drawable.ic_logo_white)
            .setColor(colorFrom(R.color.colorPrimaryDark))
            .setColorized(true)
            .setAutoCancel(true)
            .addAction(
                NotificationCompat.Action(
                android.R.drawable.stat_sys_download_done,
                getString(R.string.learn_more),
                PendingIntent.getBroadcast(context, getRandomPendingCode(), learnMoreIntent, NotificationUtils.getPendingIntentFlags())
            ))
            .build()

        manager.notify(IMPROVE_DETECTION_DISABLED_NOTIFICATION_ID, notification)
    }

    private fun getRandomPendingCode() = NotificationUtils.getRandomCode(500)
}