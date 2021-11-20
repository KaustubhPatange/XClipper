package com.kpstv.xclipper.service.receiver

import android.content.Context
import android.content.Intent
import com.kpstv.xclipper.extensions.elements.AbstractBroadcastReceiver
import com.kpstv.xclipper.extensions.helper.ClipboardLogDetector
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.Notifications
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootCompleteReceiver : AbstractBroadcastReceiver() {

    @Inject lateinit var appSettings: AppSettings

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (appSettings.isImproveDetectionEnabled()) {
            val canDetect = ClipboardLogDetector.isDetectionCompatible(context)
            if (!canDetect) {
                appSettings.setImproveDetectionEnabled(false)
                Notifications.sendImproveDetectionDisabled(context)
            }
        }
    }

}