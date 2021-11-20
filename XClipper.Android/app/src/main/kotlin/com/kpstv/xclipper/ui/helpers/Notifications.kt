package com.kpstv.xclipper.ui.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.service.receiver.AppBroadcastReceiver
import com.kpstv.xclipper.ui.activities.Start
import java.io.File
import java.util.*
import com.kpstv.xclipper.extensions.utils.SizeUtils

object Notifications {
    const val CHANNEL_ID = "my_channel_01"
    const val CHANNEL_UPDATE = "update_channel"

    private const val UPDATE_REQUEST_CODE = 129

    private const val ACCESSIBILITY_NOTIFICATION_ID = 34
    private const val UPDATE_PROGRESS_NOTIFICATION_ID = 21
    private const val UPDATE_NOTIFICATION_ID = 7
    private const val IMPROVE_DETECTION_DISABLED_NOTIFICATION_ID = 35

    private lateinit var manager: NotificationManager

    fun initialize(context: Context) = with(context) {
        Log.e("NotificationHelper", "Creating channel")
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_UPDATE,
                    getString(R.string.channel_update),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    fun sendNotification(context: Context, title: String, message: String): Unit = with(context) {
        val randomCode = getRandomNumberCode()
        val openIntent = PendingIntent.getBroadcast(
            context,
            getRandomPendingCode(),
            AppBroadcastReceiver.createOpenAppAction(context, randomCode),
            0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_clip)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .build()

        manager.notify(randomCode, notification)
    }

    fun pushNotification(context: Context, text: String, withActions: Boolean = true): Unit = with(context) {
        val randomCode = getRandomNumberCode()
        val openIntent = PendingIntent.getBroadcast(
            context,
            getRandomPendingCode(),
            AppBroadcastReceiver.createOpenAppAction(this, randomCode),
            0
        )

        val deleteIntent = AppBroadcastReceiver.createDeleteAction(context, text, randomCode)

        val specialIntent = AppBroadcastReceiver.createSmartOptionsAction(this, text)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_clip)
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

    fun sendAccessibilityDisabledNotification(context: Context): Unit = with(context) {
        val openIntent = PendingIntent.getBroadcast(
            this,
            getRandomNumberCode(),
            Intent(this, AppBroadcastReceiver::class.java).apply {
                action = AppBroadcastReceiver.ACTION_OPEN_ACCESSIBILITY
            },
            0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_clip)
            .setContentTitle(getString(R.string.clipboard_disabled_text))
            .setContentText(getString(R.string.clipboard_disabled_content))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentIntent(openIntent)
            .build()

        manager.notify(ACCESSIBILITY_NOTIFICATION_ID, notification)
    }

    fun sendUpdateProgressNotification(
        context: Context,
        currentBytes: Long,
        totalBytes: Long,
        fileName: String,
        cancelRequestCode: Int
    ) = with(context) {
        val cancelIntent = Intent(this, AppBroadcastReceiver::class.java).apply {
            action = AppBroadcastReceiver.ACTION_STOP_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(this, cancelRequestCode, cancelIntent, getMutableFlags())
        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATE)
            .setContentTitle(fileName)
            .setContentText("${SizeUtils.getSizePretty(currentBytes, false)} / ${SizeUtils.getSizePretty(totalBytes)}")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(
                100,
                ((currentBytes * 100) / totalBytes).toInt(),
                false
            )
            .setShowWhen(false)
            .addAction(R.drawable.ic_close, getString(R.string.close), pendingIntent)
            .build()

        manager.notify(UPDATE_PROGRESS_NOTIFICATION_ID, notification)
    }

    fun removeUpdateProgressNotification() = manager.cancel(UPDATE_PROGRESS_NOTIFICATION_ID)

    fun sendDownloadCompleteNotification(context: Context, file: File) = with(context) {
        val installIntent = Intent(this, AppBroadcastReceiver::class.java).apply {
            action = AppBroadcastReceiver.ACTION_INSTALL_APK
            putExtra(AppBroadcastReceiver.ARGUMENT_INSTALL_APK_FILE, file.absolutePath)
        }

        val pendingIntent = PendingIntent.getBroadcast(this, getRandomNumberCode(), installIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.update_download_complete))
            .setContentText(getString(R.string.update_download_install2))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .addAction(NotificationCompat.Action(
                android.R.drawable.stat_sys_download_done,
                getString(R.string.update_download_install_button),
                pendingIntent
            ))

        manager.notify(UPDATE_NOTIFICATION_ID,  notification.build())
    }

    fun sendUpdateAvailableNotification(context: Context) = with(context) {
        val updateIntent = Intent(this, Start::class.java)
            .apply {
                action = ActivityIntentHelper.ACTION_FORCE_CHECK_UPDATE
            }
        val pendingIntent = PendingIntent.getActivity(
            this,
            UPDATE_REQUEST_CODE,
            updateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name_full))
            .setContentText(getString(R.string.update_message))
            .setSmallIcon(R.drawable.ic_clip)
            .setColor(colorFrom(R.color.colorPrimaryDark))
            .setColorized(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    fun sendImproveDetectionDisabled(context: Context) = with(context) {
        val learnMoreIntent = AppBroadcastReceiver.createOpenUrlAction(this, getString(R.string.app_docs_improve_detect))

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Improve clipboard detection disabled")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It looks like the system has disabled the improve clipboard detection mechanism. You need to again configure it using ADB"))
            .setSmallIcon(R.drawable.ic_clip)
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

    private fun getMutableFlags() = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0

    fun getRandomNumberCode() = Random().nextInt(400) + 150
    private fun getRandomPendingCode() = Random().nextInt(400) + 550
}