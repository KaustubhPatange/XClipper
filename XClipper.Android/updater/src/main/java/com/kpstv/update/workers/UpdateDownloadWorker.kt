package com.kpstv.update.workers

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.kpstv.update.Release
import com.kpstv.update.Updater
import com.kpstv.update.internals.Streamer
import java.io.File

/**
 * Extend the class, implement the methods & schedule the worker using the static method.
 */
abstract class UpdateDownloadWorker(
    private val appContext: Context,
    workerParameters: WorkerParameters
) :
    CoroutineWorker(appContext, workerParameters) {
    open fun createDestinationFile(): File {
        val url = inputData.getString(UPDATE_URL)
            ?: throw IllegalStateException("Update url cannot be empty")
        return Updater.createUpdateFile(appContext, url)
    }

    abstract fun onProgressChange(currentBytes: Long, totalBytes: Long)
    abstract fun onCancelled()
    abstract fun onDownloadComplete(file: File)

    private var streamer: Streamer? = null

    fun getUpdateTagName(): String? {
        return inputData.getString(UPDATE_TAG)
    }

    override suspend fun doWork(): Result {
        val url = inputData.getString(UPDATE_URL) ?: return Result.failure()
        val file = createDestinationFile()
        streamer = Streamer(
            onProgressChange = { currentBytes, totalBytes ->
                if (isStopped) {
                    streamer?.stop()
                    onCancelled()
                    return@Streamer
                }
                onProgressChange(currentBytes, totalBytes)
            },
            onComplete = { onDownloadComplete(file) }
        )
        streamer?.write(url, file)

        return Result.success()
    }

    companion object {
        const val UPDATE_URL = "com.kpstv.update:UPDATE_URL"
        const val UPDATE_TAG = "com.kpstv.update:UPDATE_TAG"

        inline fun <reified T : UpdateDownloadWorker> schedule(context: Context, release: Release, uniqueWorkName: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val downloadApkUrl = release.assets.firstOrNull { it.browserDownloadUrl.endsWith(".apk") }?.browserDownloadUrl

            val updateWork = OneTimeWorkRequestBuilder<T>()
                .setInputData(
                    workDataOf(
                        UPDATE_URL to downloadApkUrl,
                        UPDATE_TAG to release.tagName
                    )
                )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.KEEP, updateWork)
        }

        fun stop(context: Context, uniqueWorkName: String) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(uniqueWorkName)
        }
    }
}