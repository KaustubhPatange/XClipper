package com.kpstv.xclipper.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kpstv.xclipper.ui.helpers.AddOnsHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

// A worker that checks if the extensions are active or not & can be run
// through different variants like schedule() & scheduleOnce()
@HiltWorker
class ExtensionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        AddOnsHelper.verifyExtensions(appContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_PERIODIC_ID = "xclipper_extension_worker"
        private const val UNIQUE_ID = "xclipper_extension_onetime_worker"
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<ExtensionWorker>(
                10, TimeUnit.HOURS, 10, TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_PERIODIC_ID, ExistingPeriodicWorkPolicy.REPLACE, request)
        }

        fun scheduleForOnce(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = OneTimeWorkRequestBuilder<ExtensionWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_ID, ExistingWorkPolicy.REPLACE, request)
        }
    }
}