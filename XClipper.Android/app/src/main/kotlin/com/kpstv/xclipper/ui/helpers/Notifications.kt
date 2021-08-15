package com.kpstv.xclipper.ui.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DELETE
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.App.ACTION_OPEN_APP
import com.kpstv.xclipper.App.ACTION_SMART_OPTIONS
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.App.NOTIFICATION_CODE
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.service.AppBroadcastReceiver
import com.kpstv.xclipper.ui.activities.Start
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

object Notifications {
    const val CHANNEL_ID = "my_channel_01"
    const val CHANNEL_UPDATE = "update_channel"

    private const val UPDATE_REQUEST_CODE = 129

    private const val ACCESSIBILITY_NOTIFICATION_ID = 34
    private const val UPDATE_PROGRESS_NOTIFICATION_ID = 21
    private const val UPDATE_NOTIFICATION_ID = 7

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
            Intent(context, AppBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_APP
                putExtra(NOTIFICATION_CODE, randomCode)
             },
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
            Intent(context, AppBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_APP
                putExtra(NOTIFICATION_CODE, randomCode)
            },
            0
        )

        val deleteIntent = Intent(context, AppBroadcastReceiver::class.java).apply {
            putExtra(APP_CLIP_DATA, text)
            putExtra(NOTIFICATION_CODE, randomCode)
            action = ACTION_DELETE
        }

        val specialIntent = Intent(context, AppBroadcastReceiver::class.java).apply {
            putExtra(APP_CLIP_DATA, text)
            action = ACTION_SMART_OPTIONS
        }

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
            .setContentText("${Utils.getSizePretty(currentBytes, false)} / ${Utils.getSizePretty(totalBytes)}")
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

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.update_download_complete))
            .setContentText(getString(R.string.update_downoad_install2))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(PendingIntent.getBroadcast(this, getRandomNumberCode(), installIntent, 0))
            .setAutoCancel(true)

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

    private fun getMutableFlags() = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0

    fun getRandomNumberCode() = Random().nextInt(400) + 150
    private fun getRandomPendingCode() = Random().nextInt(400) + 550
}