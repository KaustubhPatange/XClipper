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
import com.kpstv.xclipper.service.AppBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "my_channel_01"
        private const val ACCESSIBILITY_NOTIFICATION_ID = 34
    }

    private lateinit var manager: NotificationManager

    fun createChannel() = with(context) {
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
        }
    }

    fun sendNotification(title: String, message: String): Unit = with(context) {
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

        if (!::manager.isInitialized) createChannel()
        manager.notify(randomCode, notification)
    }

    fun pushNotification(text: String, withActions: Boolean = true): Unit = with(context) {
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

        if (!::manager.isInitialized) createChannel()
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

        if (!::manager.isInitialized) createChannel()
        manager.notify(ACCESSIBILITY_NOTIFICATION_ID, notification)
    }

    private fun getRandomNumberCode() = Random().nextInt(400) + 150
    private fun getRandomPendingCode() = Random().nextInt(400) + 550
}