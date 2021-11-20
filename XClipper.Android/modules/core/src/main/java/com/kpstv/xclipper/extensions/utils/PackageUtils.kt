package com.kpstv.xclipper.extensions.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object PackageUtils {

    data class AppPkg(
        val label: CharSequence?,
        val packageName: CharSequence?
    )

    fun isPackageInstalled(
        context: Context,
        packageName: String
    ): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Call this function whenever you want to work with installed apps.
     */
    suspend fun retrievePackageList(context: Context): List<AppPkg> = with(context) {
        suspendCoroutine { r ->
            val result = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .asSequence()
                .filter {
                    it.flags != ApplicationInfo.FLAG_SYSTEM
                }
                .mapNotNull {
                    AppPkg(
                        it.loadLabel(packageManager),
                        it.packageName
                    )
                }
                .filter { it.label != null && !it.label.contains(".") && it.packageName != null }
                .sortedBy { it.label.toString() }
                .distinctBy { it.label.toString() }.toList()
            r.resume(result)
        }
    }
}