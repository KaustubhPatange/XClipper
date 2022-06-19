package com.kpstv.xclipper.service.receiver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_CLOSE_SYSTEM_DIALOGS
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.elements.AbstractBroadcastReceiver
import com.kpstv.xclipper.extensions.launchInIO
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SpecialActionsReceiver : AbstractBroadcastReceiver() {

    @Inject lateinit var repository: MainRepository
    @Inject lateinit var appSettings: AppSettings
    @Inject lateinit var clipboardProvider: ClipboardProvider

    companion object {
        private const val ACTION_CLIP_DELETE = "com.kpstv.xclipper.clip_delete"

        private const val APP_CLIP_DATA = "com.kpstv.xclipper.clip_data"
        private const val NOTIFICATION_CODE = "com.kpstv.xclipper.sp.notification_code"

        fun createDeleteAction(context: Context, data: String, notificationId: Int) : Intent {
            return Intent(context, SpecialActionsReceiver::class.java).apply {
                putExtra(APP_CLIP_DATA, data)
                putExtra(NOTIFICATION_CODE, notificationId)
                action = ACTION_CLIP_DELETE
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val data = intent.getStringExtra(APP_CLIP_DATA)
        val notifyId = intent.getIntExtra(NOTIFICATION_CODE, -1)

        when(intent.action) {
            ACTION_CLIP_DELETE -> {
                launchInIO {
                    repository.deleteClip(data)
                }

                if (appSettings.isClipboardClearEnabled() && clipboardProvider.getCurrentClip().value == data) {
                    clipboardProvider.clearClipboard()
                }

                dismissNotification(context, notifyId)
                collapseStatusBar(context)
            }
        }
    }

    private fun dismissNotification(context: Context, notificationId: Int) {
        if (notificationId != -1) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }

    private fun collapseStatusBar(context: Context) {
        context.sendBroadcast(Intent(ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}