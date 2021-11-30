package com.kpstv.xclipper.ui.helpers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.utils.NotificationUtils
import com.kpstv.xclipper.service.receiver.ClipboardBroadcastReceiver
import com.kpstv.xclipper.service.receiver.ImproveDetectionReceiver
import com.kpstv.xclipper.service.receiver.SpecialActionsReceiver
import java.util.*

object Notifications {
    private const val CHANNEL_ID = CoreNotifications.CHANNEL_ID


    private const val IMPROVE_DETECTION_DISABLED_NOTIFICATION_ID = 35

    private lateinit var manager: NotificationManager

    fun initialize(context: Context) = with(context) {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun sendNotification(context: Context, title: String, message: String): Unit = with(context) {
        val randomCode = NotificationUtils.getRandomCode()
        val openIntent = PendingIntent.getBroadcast(
            context,
            getRandomPendingCode(),
            ClipboardBroadcastReceiver.createOpenAppAction(context, randomCode),
            NotificationUtils.getPendingIntentFlags()
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_white)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .build()

        manager.notify(randomCode, notification)
    }

    fun sendClipboardCopiedNotification(context: Context, text: String, withActions: Boolean = true): Unit = with(context) {
        val randomCode = NotificationUtils.getRandomCode()
        val openIntent = PendingIntent.getBroadcast(
            context,
            getRandomPendingCode(),
            ClipboardBroadcastReceiver.createOpenAppAction(this, randomCode),
            NotificationUtils.getPendingIntentFlags()
        )

        val deleteIntent = SpecialActionsReceiver.createDeleteAction(context, text, randomCode)

        val specialIntent = SpecialActionsReceiver.createSmartOptionsAction(this, text)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_white)
            .setContentTitle(getString(R.string.clip_content))
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentIntent(openIntent)

        if (withActions) {
            notificationBuilder.addAction(
                R.drawable.ic_delete_white,
                context.getString(R.string.delete),
                PendingIntent.getBroadcast(context, getRandomPendingCode(), deleteIntent, 0)
            )
            notificationBuilder.addAction(
                R.drawable.ic_special,
                getString(R.string.more_actions),
                PendingIntent.getBroadcast(context, getRandomPendingCode(), specialIntent, 0)
            )
        }

        manager.notify(randomCode, notificationBuilder.build())
    }

    fun sendImproveDetectionDisabled(context: Context) = with(context) {
        val learnMoreIntent = ImproveDetectionReceiver.createOpenUrlAction(this, getString(R.string.app_docs_improve_detect))

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.improve_detection_notify_title))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.improve_detection_notify_text)))
            .setSmallIcon(R.drawable.ic_logo_white)
            .setColor(colorFrom(R.color.colorPrimaryDark))
            .setColorized(true)
            .setAutoCancel(true)
            .addAction(NotificationCompat.Action(
                android.R.drawable.stat_sys_download_done,
                getString(R.string.update_download_install_button),
                PendingIntent.getBroadcast(context, getRandomPendingCode(), learnMoreIntent, 0)
            ))
            .build()

        manager.notify(IMPROVE_DETECTION_DISABLED_NOTIFICATION_ID, notification)
    }


    private fun getRandomPendingCode() = Random().nextInt(400) + 550
}