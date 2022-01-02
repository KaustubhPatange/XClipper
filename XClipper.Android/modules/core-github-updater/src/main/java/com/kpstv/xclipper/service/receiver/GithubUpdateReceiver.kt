package com.kpstv.xclipper.service.receiver

import android.content.Context
import android.content.Intent
import com.kpstv.xclipper.github_updater.R
import com.kpstv.update.Updater
import com.kpstv.xclipper.extensions.elements.AbstractBroadcastReceiver
import com.kpstv.xclipper.service.worker.GithubUpdateWorker
import es.dmoral.toasty.Toasty
import java.io.File

class GithubUpdateReceiver : AbstractBroadcastReceiver() {
    companion object {
        private const val ACTION_STOP_UPDATE = "com.kpstv.action_stop_update"

        private const val ACTION_INSTALL_APK = "com.kpstv.action_install_apk"
        private const val ARGUMENT_INSTALL_APK_FILE = "com.kpstv.action_install_apk:arg_apk_file"

        fun createStopUpdate(context: Context): Intent = Intent(context, GithubUpdateReceiver::class.java).apply {
            action = ACTION_STOP_UPDATE
        }

        fun createInstallApk(context: Context, file: File) : Intent = Intent(context, GithubUpdateReceiver::class.java).apply {
            action = ACTION_INSTALL_APK
            putExtra(ARGUMENT_INSTALL_APK_FILE, file.absolutePath)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_STOP_UPDATE -> {
                GithubUpdateWorker.stop(context)
            }
            ACTION_INSTALL_APK -> {
                val filePath = intent.getStringExtra(ARGUMENT_INSTALL_APK_FILE)
                if (filePath != null) {
                    Updater.installUpdate(context, File(filePath))
                } else {
                    Toasty.error(context, context.getString(R.string.update_error)).show()
                }
            }
        }
    }
}