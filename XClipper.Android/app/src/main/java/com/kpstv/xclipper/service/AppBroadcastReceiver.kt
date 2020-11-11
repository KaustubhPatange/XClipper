package com.kpstv.xclipper.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.widget.Toast
import com.kpstv.xclipper.App.ACTION_OPEN_APP
import com.kpstv.xclipper.App.ACTION_SMART_OPTIONS
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.activities.Main
import com.kpstv.xclipper.ui.dialogs.SpecialDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MainRepository

    private val TAG = javaClass.simpleName

    companion object {
        const val ACTION_OPEN_ACCESSIBILITY = "com.kpstv.action_open_accessibility"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null) {

            val data = intent.getStringExtra(APP_CLIP_DATA)
            val data1 = intent.data

            when (intent.action) {
                ACTION_OPEN_APP -> {
                    context.startActivity(
                        Intent(context, Main::class.java).apply {
                            flags = FLAG_ACTIVITY_BROUGHT_TO_FRONT or FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                    dismissNotification(context)
                }
                ACTION_DELETE -> {
                    repository.deleteClip(data)

                    dismissNotification(context)
                    collapseStatusBar(context)
                }
                ACTION_SMART_OPTIONS -> {

                    val newIntent = Intent(context, SpecialDialog::class.java).apply {
                        flags = FLAG_ACTIVITY_NEW_TASK
                        setData(data1)
                        putExtra(APP_CLIP_DATA, data)
                    }
                    context.startActivity(newIntent)

                    collapseStatusBar(context)
                }
                ACTION_OPEN_ACCESSIBILITY -> {
                    Utils.openAccessibility(context)
                    Toast.makeText(context, "Opening accessibility", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun dismissNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    private fun collapseStatusBar(context: Context) {
        context.sendBroadcast(Intent(ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}