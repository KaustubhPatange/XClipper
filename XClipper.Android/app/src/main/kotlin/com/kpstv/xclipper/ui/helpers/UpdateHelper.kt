package com.kpstv.xclipper.ui.helpers

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.kpstv.hvlog.HVLog
import com.kpstv.update.Updater
import com.kpstv.update.workers.UpdateDownloadWorker
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.WebSettings
import com.kpstv.xclipper.data.model.WebSettingsConverter
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import com.kpstv.xclipper.extensions.utils.asString
import com.kpstv.xclipper.service.worker.GithubCheckWorker
import com.kpstv.xclipper.service.worker.GithubUpdateWorker
import com.kpstv.xclipper.ui.dialogs.CustomLottieDialog
import com.kpstv.xclipper.ui.fragments.Home
import kotlinx.coroutines.launch
import java.io.File

/**
 * A class created to manage updates. I created this to prevent my Home fragment
 * for holding less responsibility.
 */
class UpdateHelper(
    private val activity: FragmentActivity,
    private val retrofitUtils: RetrofitUtils
) : AbstractFragmentHelper<Home>(activity, Home::class) {
    companion object {
        const val SETTINGS_URL = "https://github.com/KaustubhPatange/XClipper/raw/master/XClipper.Android/settings.json"
        private const val REPO_OWNER = "KaustubhPatange"
        private const val REPO_NAME = "XClipper"

        fun createUpdater() : Updater {
            return Updater.Builder()
                .setCurrentAppVersion(BuildConfig.VERSION_NAME)
                .setRepoOwner(REPO_OWNER)
                .setRepoName(REPO_NAME)
                .create()
        }
    }

    private var appUpdateManager: AppUpdateManager? = null
    private val githubUpdater: Updater = createUpdater()

    override fun onFragmentViewCreated() {
        GithubCheckWorker.schedule(activity)
        activity.lifecycleScope.launch {
            initialize()
        }
    }

    override fun onFragmentResumed() {
        registerCallbackOnResume()
    }

    private suspend fun initialize() {
        val responseString = retrofitUtils.fetch(SETTINGS_URL).getOrNull()?.asString()
        val webSettings = WebSettingsConverter.fromStringToWebSettings(responseString) ?: WebSettings()

        if (webSettings.useNewUpdater) {
            checkForUpdatesFromGithub()
        } else {
            checkForUpdatesFromGooglePlay()
        }
    }

    suspend fun checkForUpdatesFromGithub() {
        githubUpdater.fetch(
            onUpdateAvailable = { release ->
                showUpdateDialog(
                    onUpdateClick = {
                        UpdateDownloadWorker
                            .schedule<GithubUpdateWorker>(activity, release, GithubUpdateWorker.UNIQUE_WORK_NAME)
                    }
                )
            }
        )
    }

    private fun checkForUpdatesFromGooglePlay(): Unit = with(activity) {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateManager.registerListener(listener)
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    com.kpstv.xclipper.App.UPDATE_REQUEST_CODE
                )
            }
        }

        this@UpdateHelper.appUpdateManager = appUpdateManager
    }

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            HVLog.d(m = "Bytes Downloaded: $bytesDownloaded, TotalBytes: $totalBytesToDownload")
        } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
            notifyUpdateDownloadComplete()
        }
    }

    private fun notifyUpdateDownloadComplete() = with(activity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.update_download_complete))
            .setMessage(getString(R.string.update_downoad_install))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                appUpdateManager?.completeUpdate()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * If in between user put the app to background during update
     * download, we will inform them as download completes when
     * it is brought to foreground again.
     */
    private fun registerCallbackOnResume() {
        if (appUpdateManager == null) {
            checkForUpdatesFromGooglePlay()
        }
        appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    notifyUpdateDownloadComplete()
                }
            }
    }

    private fun showUpdateDialog(onUpdateClick: () -> Unit) {
        CustomLottieDialog(activity)
            .setLottieView(R.raw.update)
            .setTitle(activity.getString(R.string.update_available))
            .setMessage(activity.getString(R.string.update_available_text))
            .setNeutralButton(R.string.later)
            .setPositiveButton(R.string.update_button, onUpdateClick)
            .show()
    }
}
