package com.kpstv.xclipper.ui.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.utils.NotificationUtils
import com.kpstv.xclipper.extensions.utils.SizeUtils
import com.kpstv.xclipper.github_updater.R
import com.kpstv.xclipper.service.receiver.GithubUpdateReceiver
import java.io.File

object UpdaterNotifications {

    private const val CHANNEL_UPDATE_PROGRESS = "update_progress_channel"
    private const val CHANNEL_UPDATE = "update_channel"

    private const val UPDATE_REQUEST_CODE = 129
    private const val UPDATE_PROGRESS_NOTIFICATION_ID = 21
    private const val UPDATE_NOTIFICATION_ID = 7

    private lateinit var manager: NotificationManager

    fun initialize(context: Context) = with(context) {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_UPDATE,
                    getString(R.string.channel_update),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_UPDATE_PROGRESS,
                    getString(R.string.channel_update_progress),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    fun sendUpdateProgressNotification(
        context: Context,
        currentBytes: Long,
        totalBytes: Long,
        fileName: String,
        cancelRequestCode: Int
    ) = with(context) {
        val cancelIntent = GithubUpdateReceiver.createStopUpdate(this)
        val pendingIntent = PendingIntent.getBroadcast(this, cancelRequestCode, cancelIntent,
            NotificationUtils.getPendingIntentFlags()
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATE_PROGRESS)
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
            .addAction(R.drawable.updater_ic_cross, getString(android.R.string.cancel), pendingIntent)
            .build()

        manager.notify(UPDATE_PROGRESS_NOTIFICATION_ID, notification)
    }

    fun removeUpdateProgressNotification() = manager.cancel(UPDATE_PROGRESS_NOTIFICATION_ID)

    fun sendDownloadCompleteNotification(context: Context, file: File) = with(context) {
        val installIntent = GithubUpdateReceiver.createInstallApk(context, file)

        val pendingIntent = PendingIntent.getBroadcast(this, NotificationUtils.getRandomCode(), installIntent, NotificationUtils.getPendingIntentFlags())

        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATE)
            .setContentTitle(getString(R.string.update_download_complete))
            .setContentText(getString(R.string.update_download_install2))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .addAction(
                NotificationCompat.Action(
                android.R.drawable.stat_sys_download_done,
                getString(R.string.update_download_install_button),
                pendingIntent
            ))

        manager.notify(UPDATE_NOTIFICATION_ID,  notification.build())
    }

    fun sendUpdateAvailableNotification(context: Context) = with(context) {
        val updateIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            action = GithubUpdater.ACTION_FORCE_CHECK_UPDATE
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            UPDATE_REQUEST_CODE,
            updateIntent,
            NotificationUtils.getPendingIntentFlags()
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATE)
            .setContentTitle(getString(R.string.app_name_full))
            .setContentText(getString(R.string.update_message))
            .setSmallIcon(R.drawable.ic_logo_white)
            .setColor(getColorAttr(R.attr.colorAccent))
            .setColorized(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(UPDATE_NOTIFICATION_ID, notification)
    }
}