package com.kpstv.xclipper.service.receiver

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.kpstv.xclipper.core_clipboard.R
import com.kpstv.xclipper.extensions.elements.AbstractBroadcastReceiver
import com.kpstv.xclipper.extensions.utils.ClipboardUtils

internal class ClipboardBroadcastReceiver : AbstractBroadcastReceiver() {

    private val TAG = javaClass.simpleName

    companion object {
        private const val ACTION_OPEN_ACCESSIBILITY = "com.kpstv.action_open_accessibility"

        fun createAccessibilityServiceOpenAction(context: Context) : Intent {
            return Intent(context, ClipboardBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_ACCESSIBILITY
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_OPEN_ACCESSIBILITY -> {
                ClipboardUtils.openServiceAccessibilitySetting(context)
                Toast.makeText(context, context.getString(R.string.open_accessibility), Toast.LENGTH_SHORT).show()
            }
        }
    }
}