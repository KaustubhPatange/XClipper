package com.kpstv.xclipper.ui.helpers

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.R
import com.kpstv.xclipper.di.action.SpecialActionOption
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.utils.NotificationUtils
import com.kpstv.xclipper.extensions.utils.ToastyUtils
import com.kpstv.xclipper.service.receiver.SpecialActionsReceiver
import com.kpstv.xclipper.ui.activities.SpecialActions
import java.util.*

object Notifications {
    private const val CHANNEL_ID = CoreNotifications.CHANNEL_ID

    private val manager: NotificationManager get() = CoreNotifications.getNotificationManager()

    fun init(context: ComponentActivity) = with(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                    if (!result) {
                        ToastyUtils.showWarning(this, getString(R.string.err_notification_permission))
                    }
                }
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun sendClipboardCopiedNotification(
        context: Context,
        text: String,
        withCopyButton: Boolean = false,
        withSpecialActions: Boolean = true
    ): Unit = with(context) {
        val notificationId = text.hashCode()

        val deleteIntent = SpecialActionsReceiver.createDeleteAction(context, text, notificationId)
        val copyIntent = SpecialActionsReceiver.createCopyAction(context, text, notificationId)

        val specialIntent =
            SpecialActions.launchIntent(context, text, SpecialActionOption(showShareOption = true))

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_white)
            .setContentTitle(getString(R.string.clip_content))
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(getColorAttr(R.attr.colorAccent))
            .setContentIntent(NotificationUtils.getAppLaunchPendingIntent(this))

        if (withCopyButton) {
            notificationBuilder.addAction(
                R.drawable.ic_copy,
                context.getString(android.R.string.copy),
                PendingIntent.getBroadcast(
                    context,
                    getRandomPendingCode(),
                    copyIntent,
                    NotificationUtils.getPendingIntentFlags()
                )
            )
        }
        if (withSpecialActions) {
            notificationBuilder.addAction(
                R.drawable.ic_delete_white,
                context.getString(R.string.delete),
                PendingIntent.getBroadcast(
                    context,
                    getRandomPendingCode(),
                    deleteIntent,
                    NotificationUtils.getPendingIntentFlags()
                )
            )
            notificationBuilder.addAction(
                R.drawable.ic_special,
                getString(R.string.more_actions),
                PendingIntent.getActivity(
                    context,
                    getRandomPendingCode(),
                    specialIntent,
                    NotificationUtils.getPendingIntentFlags()
                )
            )
        }

        manager.notify(notificationId, notificationBuilder.build())
    }

    private fun getRandomPendingCode() = Random().nextInt(400) + 550
}