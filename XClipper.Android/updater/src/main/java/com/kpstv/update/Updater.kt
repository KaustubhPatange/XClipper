package com.kpstv.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Environment
import com.kpstv.update.Updater.UpdateLogic
import com.kpstv.update.internals.UpdaterBroadcast
import okhttp3.OkHttpClient
import okhttp3.internal.toImmutableList
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Updater based on Github releases
 */
class Updater private constructor() {
    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val config = Config()

    @JvmSynthetic
    suspend fun fetch(
        onUpdateCheckFailed: (Throwable) -> Unit = {  it.printStackTrace() },
        onUpdateNotAvailable: () -> Unit = {},
        onUpdateAvailable: (Release) -> Unit,
    ) {
        val result = client.get("https://api.github.com/repos/${config.githubRepoOwner}/${config.githubRepoName}/releases")
        result.onSuccess { response ->
            val body = response.body?.string() ?: return@onSuccess
            response.close()

            val releases = ReleaseListConverter.fromStringToRelease(body)?.toImmutableList() ?: return@onSuccess
            val updates = releases.filter { r -> r.assets.any { it.browserDownloadUrl.endsWith(".apk") } }

            if (updates.isEmpty()) return@onSuccess

            val update = config.customUpdateLogic.onCheck(config.currentAppVersion, updates)
            if (update != null) {
                onUpdateAvailable(update)
            } else {
                onUpdateNotAvailable()
            }
        }
        result.onFailure { onUpdateCheckFailed(it) }
    }

    class Builder {
        private val updater = Updater()

        fun setCurrentAppVersion(value: String): Builder {
            updater.config.currentAppVersion = value
            return this
        }

        fun setRepoOwner(value: String): Builder {
            updater.config.githubRepoOwner = value
            return this
        }

        fun setRepoName(value: String): Builder {
            updater.config.githubRepoName = value
            return this
        }

        fun setUpdateLogic(value: UpdateLogic): Builder {
            updater.config.customUpdateLogic = value
            return this
        }

        fun create(): Updater {
            check(updater.config.githubRepoName.isNotEmpty()) { throw IllegalStateException("Repo name cannot be empty") }
            check(updater.config.githubRepoOwner.isNotEmpty()) { throw IllegalStateException("Repo owner cannot be empty") }

            if (updater.config.currentAppVersion.isEmpty() && updater.config.customUpdateLogic === defaultUpdateLogic) {
                throw IllegalStateException("App version cannot be empty for default update logic")
            }

            return updater
        }
    }


    fun interface UpdateLogic {
        fun onCheck(appVersion: String, items: List<Release>): Release?
    }

    private data class Config(
        var currentAppVersion: String = "",
        var githubRepoOwner: String = "",
        var githubRepoName: String = "",
        var customUpdateLogic: UpdateLogic = defaultUpdateLogic,
    )

    companion object {
        @JvmStatic
        fun createUpdateFile(context: Context, release: Release) : File {
            val url = release.assets.map { it.browserDownloadUrl}.find { it.endsWith(".apk") }!!
            return createUpdateFile(context, url)
        }

        @JvmStatic
        fun createUpdateFile(context: Context, downloadUrl: String) : File {
            val fileName = Uri.parse(downloadUrl).lastPathSegment ?: throw IllegalStateException("Could not retrieve path segment")
            return File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
        }

        @JvmStatic
        fun installUpdate(context: Context, file: File) {
            val installer = context.packageManager.packageInstaller
            val sessionId = installer.createSession(
                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            )
            val session = installer.openSession(sessionId)
            try {
                val outStream = session.openWrite("package", 0, file.length())
                val inputStream = file.inputStream()
                val buffer = ByteArray(16384)
                var length: Int
                while(true) {
                    length = inputStream.read(buffer)
                    if (length < 0) break
                    outStream.write(buffer, 0, length)
                }

                val intent = Intent(context, UpdaterBroadcast::class.java).apply {
                    action = UpdaterBroadcast.PACKAGE_INSTALLED_ACTION
                }
                val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

                outStream.close()
                inputStream.close()
                session.commit(pendingIntent.intentSender)
            } finally {
//                session.abandon()
            }
        }

        // matches through tag name eg: ^v[\d\.]+$
        private val defaultUpdateLogic = UpdateLogic { appVersion, items ->
            val current = appVersion.replace(".", "").toInt()
            return@UpdateLogic items.associateBy { it.tagName }
                .filterKeys { tag ->
                    val new = tag.replace("v", "").replace(".", "").toInt()
                    return@filterKeys new > current
                }
                .firstNotNullOfOrNull { it.value }
        }
    }
}