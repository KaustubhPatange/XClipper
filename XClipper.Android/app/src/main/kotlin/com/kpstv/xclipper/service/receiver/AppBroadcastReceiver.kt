package com.kpstv.xclipper.service.receiver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.widget.Toast
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.elements.AbstractBroadcastReceiver
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.activities.Start
import com.kpstv.xclipper.ui.utils.LaunchUtils

class AppBroadcastReceiver : AbstractBroadcastReceiver() {

    private val TAG = javaClass.simpleName

    companion object {
        const val ACTION_OPEN_ACCESSIBILITY = "com.kpstv.action_open_accessibility"

        private const val ACTION_OPEN_URL = "com.kpstv.action_open_url"
        private const val ARGUMENT_OPEN_URL_LINK = "com.kpstv.action_open_url:arg_link"
        private const val ACTION_OPEN_APP = "com.kpstv.xclipper.open_app"
        private const val NOTIFICATION_CODE = "com.kpstv.xclipper.abr.notification_code"


        fun createOpenUrlAction(context: Context, url: String) : Intent {
            return Intent(context, AppBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_URL
                putExtra(ARGUMENT_OPEN_URL_LINK, url)
            }
        }

        fun createOpenAppAction(context: Context, notificationId: Int) : Intent {
            return Intent(context, AppBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_APP
                putExtra(NOTIFICATION_CODE, notificationId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val notifyId = intent.getIntExtra(NOTIFICATION_CODE, -1)

        when (intent.action) {
            ACTION_OPEN_APP -> {
                context.startActivity(
                    Intent(context, Start::class.java).apply {
                        flags = FLAG_ACTIVITY_BROUGHT_TO_FRONT or FLAG_ACTIVITY_NEW_TASK
                    }
                )
                dismissNotification(context, notifyId)
            }

            ACTION_OPEN_ACCESSIBILITY -> {
                Utils.openClipboardServiceAccessibility(context)
                Toast.makeText(context, context.getString(R.string.open_accessibility), Toast.LENGTH_SHORT).show()
            }
            ACTION_OPEN_URL -> {
                val url = intent.getStringExtra(ARGUMENT_OPEN_URL_LINK)
                if (url != null) {
                    LaunchUtils.commonUrlLaunch(context, url)
                }
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