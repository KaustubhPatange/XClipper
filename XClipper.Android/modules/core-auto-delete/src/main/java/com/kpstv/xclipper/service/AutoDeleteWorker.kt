package com.kpstv.xclipper.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kpstv.xclipper.data.model.ClipTagType
import com.kpstv.xclipper.data.model.DateFilter
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.model.TagFilter
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.enumerations.SpecialTagFilter
import com.kpstv.xclipper.ui.helper.AutoDeleteNotifications.createAutoDeleteProgressForegroundInfo
import com.kpstv.xclipper.ui.helper.AutoDeleteNotifications.createAutoDeleteSuccessNotification
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class AutoDeleteWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appSettings: AppSettings,
    private val mainRepository: MainRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // if not enabled then don't run
        if (!appSettings.canAutoDeleteClips())
            return Result.success()

        setForeground(createAutoDeleteProgressForegroundInfo(appContext))

        val setting = appSettings.getAutoDeleteSetting()

        val minDate = Calendar.getInstance().apply {
            val instance = Calendar.getInstance()
            set(Calendar.DAY_OF_MONTH, instance.get(Calendar.DAY_OF_MONTH) - setting.dayNumber)
        }
        val clips = mainRepository.getDataByFilter(
            dateFilter = DateFilter(minDate.time, DateFilter.Type.LESS_THAN),
            tagFilter = TagFilter(
                tags = setting.excludeTags.map { Tag.from(it, ClipTagType.SYSTEM_TAG) },
                type = TagFilter.Type.EXCLUDE
            ),
            specialTagFilters = buildList {
                if (setting.shouldDeletePinnedClip) add(SpecialTagFilter.Pin)
            }
        )

        if (clips.isNotEmpty()) {
            mainRepository.deleteMultiple(
                clips = clips,
                deleteType = if (setting.shouldDeleteRemoteClip)
                    MainRepository.DeleteType.ALL
                else
                    MainRepository.DeleteType.LOCAL)

            createAutoDeleteSuccessNotification(appContext, clips.size, setting.dayNumber)
        }

        return Result.success()
    }

    companion object {
        private const val UNIQUE_PERIODIC_ID = "xclipper_auto_delete_worker"
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .build()

            val request = PeriodicWorkRequestBuilder<AutoDeleteWorker>(
                2, TimeUnit.HOURS, 10, TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_PERIODIC_ID, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}