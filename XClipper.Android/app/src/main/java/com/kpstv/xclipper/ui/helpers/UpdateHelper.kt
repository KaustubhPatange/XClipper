package com.kpstv.xclipper.ui.helpers

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.R
import com.kpstv.xclipper.ui.fragments.Home

/**
 * A class created to manage updates. I created this to prevent my Home fragment
 * for holding less responsibility.
 */
class UpdateHelper(
    private val activity: FragmentActivity
) : AbstractFragmentHelper<Home>(activity, Home::class) {
    private lateinit var appUpdateManager: AppUpdateManager

    override fun onFragmentViewCreated() {
        checkForUpdates()
    }

    override fun onFragmentResumed() {
        registerCallbackOnResume()
    }

    private fun checkForUpdates(): Unit = with(activity) {
        appUpdateManager =
            com.google.android.play.core.appupdate.AppUpdateManagerFactory.create(this)
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
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_download_complete))
            .setMessage(getString(R.string.update_downoad_install))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                appUpdateManager.completeUpdate()
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
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    notifyUpdateDownloadComplete()
                }
            }
    }
}