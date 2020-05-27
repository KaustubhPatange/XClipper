package com.kpstv.xclipper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import androidx.core.app.NotificationManagerCompat
import com.kpstv.xclipper.App.ACTION_OPEN_APP
import com.kpstv.xclipper.App.ACTION_SMART_OPTIONS
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.ui.activities.Main
import com.kpstv.xclipper.ui.activities.Silent
import com.kpstv.xclipper.ui.helpers.NotificationHelper.Companion.NOTIFICATION_ID
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class AppBroadcastReceiver : BroadcastReceiver(), KodeinAware {

    private val TAG = javaClass.simpleName
    override lateinit var kodein: Kodein
    private val repository by instance<MainRepository>()

    override fun onReceive(context: Context?, intent: Intent?) {
        kodein = (context?.applicationContext as KodeinAware).kodein

        if (intent != null) {

            val data = intent.getStringExtra(APP_CLIP_DATA)
            val data1 = intent.data

            when (intent.action) {
                ACTION_OPEN_APP -> {
                    context.startActivity(
                        Intent(context, Main::class.java).apply {
                            flags = FLAG_ACTIVITY_BROUGHT_TO_FRONT or FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                    NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
                }
                ACTION_DELETE -> {
                    repository.deleteClip(data)
                    NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
                }
                ACTION_SMART_OPTIONS -> {

                    // TODO: If possible try to change this stupid workaround

                    val newIntent = Intent(context, Silent::class.java).apply {
                        action = ACTION_SMART_OPTIONS
                        setData(data1)
                        putExtra(APP_CLIP_DATA, data)
                        flags = FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(newIntent)

                    context.sendBroadcast(Intent(ACTION_CLOSE_SYSTEM_DIALOGS))
                }
            }
        }
    }


}