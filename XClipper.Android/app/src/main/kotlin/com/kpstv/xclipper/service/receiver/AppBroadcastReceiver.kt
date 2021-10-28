package com.kpstv.xclipper.service.receiver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.widget.Toast
import com.kpstv.update.Updater
import com.kpstv.xclipper.App.ACTION_OPEN_APP
import com.kpstv.xclipper.App.ACTION_SMART_OPTIONS
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.App.NOTIFICATION_CODE
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.AbstractBroadcastReceiver
import com.kpstv.xclipper.extensions.launchInIO
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.service.worker.GithubUpdateWorker
import com.kpstv.xclipper.ui.activities.SpecialActions
import com.kpstv.xclipper.ui.activities.Start
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AppBroadcastReceiver : AbstractBroadcastReceiver() {

    @Inject lateinit var repository: MainRepository

    private val TAG = javaClass.simpleName

    companion object {
        const val ACTION_OPEN_ACCESSIBILITY = "com.kpstv.action_open_accessibility"
        const val ACTION_STOP_UPDATE = "com.kpstv.action_stop_update"
        const val ACTION_INSTALL_APK = "com.kpstv.action_install_apk"

        const val ARGUMENT_INSTALL_APK_FILE = "com.kpstv.action_install_apk:arg_apk_file"

        private const val ACTION_OPEN_URL = "com.kpstv.action_open_url"
        private const val ARGUMENT_OPEN_URL_LINK = "com.kpstv.action_open_url:arg_link"

        fun createOpenUrlAction(context: Context, url: String) : Intent {
            return Intent(context, AppBroadcastReceiver::class.java).apply {
                action = ACTION_OPEN_URL
                putExtra(ARGUMENT_OPEN_URL_LINK, url)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val data = intent.getStringExtra(APP_CLIP_DATA)
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
            ACTION_DELETE -> {
                launchInIO {
                    repository.deleteClip(data)
                }

                dismissNotification(context, notifyId)
                collapseStatusBar(context)
            }
            ACTION_SMART_OPTIONS -> {

                if (data != null) {
                    SpecialActions.launch(context, data)
                }

                collapseStatusBar(context)
            }
            ACTION_OPEN_ACCESSIBILITY -> {
                Utils.openAccessibility(context)
                Toast.makeText(context, context.getString(R.string.open_accessibility), Toast.LENGTH_SHORT).show()
            }
            ACTION_STOP_UPDATE -> {
                GithubUpdateWorker.stop(context)
            }
            ACTION_INSTALL_APK -> {
                val filePath = intent.getStringExtra(ARGUMENT_INSTALL_APK_FILE)
                if (filePath != null) {
                    Updater.installUpdate(context, File(filePath))
                } else {
                    Toasty.error(context, context.getString(R.string.update_error)).show()
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

    private fun collapseStatusBar(context: Context) {
        context.sendBroadcast(Intent(ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}