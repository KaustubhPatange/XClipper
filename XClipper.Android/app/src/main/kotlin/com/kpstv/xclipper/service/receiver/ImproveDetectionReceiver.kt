package com.kpstv.xclipper.service.receiver

import android.content.Context
import android.content.Intent
import com.kpstv.xclipper.extensions.elements.AbstractBroadcastReceiver
import com.kpstv.xclipper.ui.utils.LaunchUtils

class ImproveDetectionReceiver : AbstractBroadcastReceiver() {
    companion object {
        private const val ACTION_OPEN_URL = "com.kpstv.action_open_url"
        private const val ARGUMENT_OPEN_URL_LINK = "com.kpstv.action_open_url:arg_link"

        fun createOpenUrlAction(context: Context, url: String) : Intent {
            return Intent(context, ClipboardBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_URL
                putExtra(ARGUMENT_OPEN_URL_LINK, url)
            }
        }
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when(intent.action) {
            ACTION_OPEN_URL -> {
                val url = intent.getStringExtra(ARGUMENT_OPEN_URL_LINK)
                if (url != null) {
                    LaunchUtils.commonUrlLaunch(context, url)
                }
            }
        }
    }
}