package com.kpstv.xclipper.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.kpstv.xclipper.App.ACTION_OPEN_APP
import com.kpstv.xclipper.App.ACTION_SMART_OPTIONS
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.App.NOTIFICATION_CODE
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.AbstractBroadcastReceiver
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.activities.SpecialActions
import com.kpstv.xclipper.ui.activities.Start
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppBroadcastReceiver : AbstractBroadcastReceiver() {

    @Inject lateinit var repository: MainRepository

    private val TAG = javaClass.simpleName

    companion object {
        const val ACTION_OPEN_ACCESSIBILITY = "com.kpstv.action_open_accessibility"
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
                Coroutines.io {
                    repository.deleteClip(data)
                }

                dismissNotification(context, notifyId)
                collapseStatusBar(context)
            }
            ACTION_SMART_OPTIONS -> {

                val newIntent = Intent(context, SpecialActions::class.java).apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                    setData(Uri.parse(data))
                    putExtra(APP_CLIP_DATA, data)
                }
                context.startActivity(newIntent)

                collapseStatusBar(context)
            }
            ACTION_OPEN_ACCESSIBILITY -> {
                Utils.openAccessibility(context)
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

    private fun collapseStatusBar(context: Context) {
        context.sendBroadcast(Intent(ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}