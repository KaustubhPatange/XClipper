package com.kpstv.xclipper.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.ui.helpers.extensions.AddOns
import com.kpstv.xclipper.ui.helpers.extensions.ExtensionHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ExtensionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferenceProvider: PreferenceProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val lists = AddOns.getAllExtensions(appContext)
        lists.forEach { item ->
            val helper = ExtensionHelper.BillingHelper(appContext, preferenceProvider, item.sku)
            helper.init() // auto check for validation.
        }

        return Result.success()
    }

    companion object {
        private const val UNIQUE_ID = "xclipper_extension_worker"
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExtensionWorker>(
                10, TimeUnit.HOURS, 10, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_ID, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}