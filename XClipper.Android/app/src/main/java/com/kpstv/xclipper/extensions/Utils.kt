package com.kpstv.xclipper.extensions

import android.app.ActivityManager
import android.content.Context

class Utils {
    companion object {
        fun isRunning(ctx: Context): Boolean {
            val activityManager =
                ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks =
                activityManager.getRunningTasks(Int.MAX_VALUE)
            for (task in tasks) {
                if ("com.kpstv.xclipper.service.ChangeClipboardActivity".equals(task.baseActivity!!.className, ignoreCase = true)
                ) return true
            }
            return false
        }
    }
}