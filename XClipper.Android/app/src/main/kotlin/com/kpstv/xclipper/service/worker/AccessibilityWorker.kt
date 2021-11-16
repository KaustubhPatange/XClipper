package com.kpstv.xclipper.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.ui.helpers.Notifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class AccessibilityWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        if (!ClipboardAccessibilityService.isRunning(appContext)) {
            Notifications.sendAccessibilityDisabledNotification(appContext)
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