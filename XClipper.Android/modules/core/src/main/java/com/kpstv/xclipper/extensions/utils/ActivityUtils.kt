package com.kpstv.xclipper.extensions.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import kotlin.reflect.KClass

object ActivityUtils {
    @Suppress("DEPRECATION")
    fun isActivityRunning(ctx: Context, clazz: KClass<out Activity>): Boolean {
        val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.getRunningTasks(Int.MAX_VALUE).any {
            it.topActivity?.className == clazz.qualifiedName
        }
    }
}