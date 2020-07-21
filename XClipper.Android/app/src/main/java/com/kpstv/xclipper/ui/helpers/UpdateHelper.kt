package com.kpstv.xclipper.ui.helpers

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.kpstv.xclipper.R
import com.kpstv.xclipper.ui.fragments.Home

/**
 * A class created to manage updates. I created this to prevent my Home fragment
 * for holding less responsibility.
 *
 * An update is not a part of UI code but still needs to be managed somehow, that's why
 * this class instance should be register on a MainActivity, where using
 * [fragmentLifeCycleCallbacks] we can track Home fragment and launch update method.
 *
 * There maybe a better code for handling this stuff, if you've let me know. At least
 * for now Home fragment is following single responsibility principle.
 */
class UpdateHelper(
    private val activity: FragmentActivity
) {
    private lateinit var appUpdateManager: AppUpdateManager

    private fun checkForUpdates() = with(activity) {
        appUpdateManager =
            com.google.android.play.core.appupdate.AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateManager.registerListener(listener)
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE,
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
            Log.e(TAG, "Bytes Downloaded: $bytesDownloaded, TotalBytes: $totalBytesToDownload")
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

    private val TAG = javaClass.simpleName

    private val fragmentLifeCycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)
            if (f is Home) // For Home Fragment only
                checkForUpdates()
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            super.onFragmentResumed(fm, f)
            if (f is Home) // For Home Fragment only
                registerCallbackOnResume()
        }
    }

    fun register() : UpdateHelper {
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
            fragmentLifeCycleCallbacks, true
        )
        return this
    }

    /**
     * Must be called when activity is destroying.
     */
    fun unregister() {
        activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(
            fragmentLifeCycleCallbacks
        )
    }
}