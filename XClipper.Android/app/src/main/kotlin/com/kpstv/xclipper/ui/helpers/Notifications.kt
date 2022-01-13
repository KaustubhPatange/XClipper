package com.kpstv.xclipper.ui.helpers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.NotificationUtils
import com.kpstv.xclipper.service.receiver.SpecialActionsReceiver
import com.kpstv.xclipper.ui.activities.SpecialActions
import java.util.*

object Notifications {
    private const val CHANNEL_ID = CoreNotifications.CHANNEL_ID

    private val manager: NotificationManager get() = CoreNotifications.getNotificationManager()

    fun sendClipboardCopiedNotification(context: Context, text: String, withSpecialActions: Boolean = true): Unit = with(context) {
        val notificationId = text.hashCode()

        val deleteIntent = SpecialActionsReceiver.createDeleteAction(context, text, notificationId)

        val specialIntent = SpecialActions.launchIntent(context, text)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_white)
            .setContentTitle(getString(R.string.clip_content))
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentIntent(NotificationUtils.getAppLaunchPendingIntent(this))

        if (withSpecialActions) {
            notificationBuilder.addAction(
                R.drawable.ic_delete_white,
                context.getString(R.string.delete),
                PendingIntent.getBroadcast(context, getRandomPendingCode(), deleteIntent, NotificationUtils.getPendingIntentFlags())
            )
            notificationBuilder.addAction(
                R.drawable.ic_special,
                getString(R.string.more_actions),
                PendingIntent.getActivity(context, getRandomPendingCode(), specialIntent, NotificationUtils.getPendingIntentFlags())
            )
        }

        manager.notify(notificationId, notificationBuilder.build())
    }

    private fun getRandomPendingCode() = Random().nextInt(400) + 550
}