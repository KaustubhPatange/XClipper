package com.kpstv.xclipper.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kpstv.update.workers.UpdateDownloadWorker
import com.kpstv.xclipper.R
import com.kpstv.xclipper.ui.helpers.Notifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class GithubUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : UpdateDownloadWorker(appContext, workerParams) {

    private val cancelRequestCode = Notifications.getRandomNumberCode()

    override fun onProgressChange(currentBytes: Long, totalBytes: Long) {
        val name = appContext.getString(R.string.update_downloading_title, getUpdateTagName())
        Notifications.sendUpdateProgressNotification(appContext, currentBytes, totalBytes, name, cancelRequestCode)
    }

    override fun onCancelled() {
        Notifications.removeUpdateProgressNotification()
    }

    override fun onDownloadComplete(file: File) {
        onCancelled()
        Notifications.sendDownloadCompleteNotification(appContext, file)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "com.kpstv.xclipper:update-worker"

        fun stop(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}