package com.kpstv.xclipper.service.receiver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.widget.Toast
import com.kpstv.xclipper.core_clipboard.R
import com.kpstv.xclipper.extensions.elements.AbstractBroadcastReceiver
import com.kpstv.xclipper.extensions.utils.ClipboardUtils

class ClipboardBroadcastReceiver : AbstractBroadcastReceiver() {

    private val TAG = javaClass.simpleName

    companion object {
        private const val ACTION_OPEN_ACCESSIBILITY = "com.kpstv.action_open_accessibility"

        private const val ACTION_OPEN_APP = "com.kpstv.xclipper.open_app"
        private const val NOTIFICATION_CODE = "com.kpstv.xclipper.abr.notification_code"

        fun createOpenAppAction(context: Context, notificationId: Int) : Intent {
            return Intent(context, ClipboardBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_APP
                putExtra(NOTIFICATION_CODE, notificationId)
            }
        }

        fun createAccessibilityServiceOpenAction(context: Context) : Intent {
            return Intent(context, ClipboardBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_ACCESSIBILITY
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val notifyId = intent.getIntExtra(NOTIFICATION_CODE, -1)

        when (intent.action) {
            ACTION_OPEN_APP -> {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    flags = FLAG_ACTIVITY_BROUGHT_TO_FRONT or FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(launchIntent)
                dismissNotification(context, notifyId)
            }
            ACTION_OPEN_ACCESSIBILITY -> {
                ClipboardUtils.openServiceAccessibilitySetting(context)
                Toast.makeText(context, context.getString(R.string.open_accessibility), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dismissNotification(context: Context, notificationId: Int) {
        if (notificationId != -1) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }
}