package com.kpstv.xclipper.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kpstv.xclipper.data.model.WebSettings
import com.kpstv.xclipper.data.model.WebSettingsConverter
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import com.kpstv.xclipper.extensions.utils.asString
import com.kpstv.xclipper.ui.helpers.Notifications
import com.kpstv.xclipper.ui.helpers.fragments.UpdateHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class GithubCheckWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val retrofitUtils: RetrofitUtils,
) : CoroutineWorker(appContext, workerParams) {

    private val githubUpdater = UpdateHelper.createUpdater()

    override suspend fun doWork(): Result {
        val responseString = retrofitUtils.fetch(UpdateHelper.SETTINGS_URL).getOrNull()?.asString()
        val webSettings = WebSettingsConverter.fromStringToWebSettings(responseString) ?: WebSettings()

        if (webSettings.useNewUpdater) {
            githubUpdater.fetch(
                onUpdateAvailable = {
                    Notifications.sendUpdateAvailableNotification(appContext)
                }
            )
        }
        return Result.failure()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "com.kpstv.xclipper:github-check-worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request =
                PeriodicWorkRequestBuilder<GithubCheckWorker>(1, TimeUnit.HOURS, 5, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, request
            )
        }
    }
}