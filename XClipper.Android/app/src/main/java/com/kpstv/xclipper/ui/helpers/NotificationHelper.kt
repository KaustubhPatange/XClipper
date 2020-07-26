package com.kpstv.xclipper.ui.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DELETE
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.App.ACTION_OPEN_APP
import com.kpstv.xclipper.App.ACTION_SMART_OPTIONS
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.R
import com.kpstv.xclipper.service.AppBroadcastReceiver

class NotificationHelper(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "my_channel_01"
        const val NOTIFICATION_ID = 23
    }

    private lateinit var manager: NotificationManager

    fun createChannel() = with(context) {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ))
        }
    }

    fun pushNotification(text: String) = with(context) {
        val openIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AppBroadcastReceiver::class.java).apply { action = ACTION_OPEN_APP },
            0
        )

        val deleteIntent = Intent(context, AppBroadcastReceiver::class.java).apply {
            data = Uri.parse(text)
            putExtra(APP_CLIP_DATA, text)
            action = ACTION_DELETE
        }

        val specialIntent = Intent(context, AppBroadcastReceiver::class.java).apply {
            data = Uri.parse(text)
            putExtra(APP_CLIP_DATA, text)
            action = ACTION_SMART_OPTIONS
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_clip)
            .setContentTitle(getString(R.string.clip_content))
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentIntent(openIntent)
            .addAction(
                R.drawable.ic_delete_white,
                context.getString(R.string.delete),
                PendingIntent.getBroadcast(context, 0, deleteIntent, 0)
            )
            .addAction(
                R.drawable.ic_special,
                getString(R.string.more_actions),
                PendingIntent.getBroadcast(context, 0, specialIntent, 0)
            ).build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}