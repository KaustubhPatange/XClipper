package com.kpstv.update.internals

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.widget.Toast

class UpdaterBroadcast : BroadcastReceiver() {
    companion object {
        const val PACKAGE_INSTALLED_ACTION = "com.kpstv.update:INSTALL_ACTION"
    }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == PACKAGE_INSTALLED_ACTION) {
            val message = intent.extras?.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)
            when(val status = intent.extras?.getInt(PackageInstaller.EXTRA_STATUS)) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val activityIntent = intent.extras!!.get(Intent.EXTRA_INTENT) as Intent
                    activityIntent.flags = activityIntent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(activityIntent)
                }
                PackageInstaller.STATUS_FAILURE_ABORTED -> {}
                PackageInstaller.STATUS_SUCCESS -> {}
                else -> {
                    println("Install failed: $status, $message")
                    Toast.makeText(context, "Install failed: $status, $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}