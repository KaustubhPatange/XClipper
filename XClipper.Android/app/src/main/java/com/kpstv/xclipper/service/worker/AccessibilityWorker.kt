package com.kpstv.xclipper.service.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AccessibilityWorker @WorkerInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    @Inject lateinit var notificationHelper: NotificationHelper

    override fun doWork(): Result {
        if (!Utils.isClipboardAccessibilityServiceRunning(appContext)) {
            notificationHelper.sendAccessibilityDisabledNotification(appContext)
        }
        return Result.success()
    }

    companion object {
        private const val UNIQUE_ID = "xclipper_accessibility_worker"
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<AccessibilityWorker>(
                20, TimeUnit.MINUTES, 5, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_ID, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}