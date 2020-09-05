package com.kpstv.xclipper.service.worker

import android.content.Context
import androidx.work.*
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.util.concurrent.TimeUnit

class AccessibilityWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params), KodeinAware {
    override var kodein: Kodein = (context.applicationContext as KodeinAware).kodein
    private val notificationHelper by instance<NotificationHelper>()

    override fun doWork(): Result {
        if (!Utils.isClipboardAccessibilityServiceRunning(context)) {
            notificationHelper.sendAccessibilityDisabledNotification(context)
        }
        return Result.success()
    }

    companion object {
        private const val UNIQUE_ID = "xclipper_accessibility_worker"
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<AccessibilityWorker>(
                20,
                TimeUnit.MINUTES,
                5,
                TimeUnit.MINUTES
            )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_ID, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}