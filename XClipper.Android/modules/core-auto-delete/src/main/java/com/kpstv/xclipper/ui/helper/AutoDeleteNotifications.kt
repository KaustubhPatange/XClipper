package com.kpstv.xclipper.ui.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.kpstv.xclipper.auto_delete.R
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.ui.helpers.CoreNotifications

object AutoDeleteNotifications {
    private const val CHANNEL_ID = "auto_delete_channel"
    private const val CORE_CHANNEL_ID = CoreNotifications.CHANNEL_ID

    private const val AUTO_DELETE_PROGRESS_NOTIFICATION_ID = 37
    private const val AUTO_DELETE_SUCCESS_NOTIFICATION_ID = 38

    private lateinit var manager: NotificationManager

    fun initialize(context: Context) = with(context) {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.ad_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    internal fun createAutoDeleteProgressForegroundInfo(context: Context): ForegroundInfo = with(context) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(getString(R.string.ad_nt_progress_title))
            .setSmallIcon(R.drawable.ic_logo_white)
            .setColor(getColorAttr(R.attr.colorAccent))
            .setProgress(100, 0, true)

        ForegroundInfo(AUTO_DELETE_PROGRESS_NOTIFICATION_ID, builder.build())
    }

    internal fun createAutoDeleteSuccessNotification(context: Context, total: Int, days: Int) : Unit = with(context) {
        val builder = NotificationCompat.Builder(this, CORE_CHANNEL_ID)
            .setContentTitle(getString(R.string.ad_nt_success_title))
            .setContentText(getString(R.string.ad_nt_success_text, total, days))
            .setStyle(NotificationCompat.BigTextStyle())
            .setSmallIcon(R.drawable.ic_logo_white)
            .setColor(getColorAttr(R.attr.colorAccent))

        manager.notify(AUTO_DELETE_SUCCESS_NOTIFICATION_ID, builder.build())
    }
}