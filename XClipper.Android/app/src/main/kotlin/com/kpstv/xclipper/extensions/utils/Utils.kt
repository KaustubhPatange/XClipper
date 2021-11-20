package com.kpstv.xclipper.extensions.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.AuthenticationHelper
import com.kpstv.xclipper.ui.helpers.FirebaseSyncHelper
import java.io.InputStream
import kotlin.reflect.KClass

class Utils {
    companion object {
        @Suppress("DEPRECATION")
        fun isActivityRunning(ctx: Context, clazz: KClass<out Activity>): Boolean {
            val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return activityManager.getRunningTasks(Int.MAX_VALUE).any {
                    it.topActivity?.className == clazz.qualifiedName
                }
        }

        /**
         * This will check if accessibility service is enabled or not.
         *
         * @param service Provide the accessibility service class of which you want to
         * detect.
         */
        fun isAccessibilityServiceEnabled(
            context: Context,
            service: Class<out AccessibilityService?>
        ): Boolean {
            val am =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            for (enabledService in enabledServices) {
                val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
                if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name
                ) return true
            }
            val accessibilityPrefs = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (accessibilityPrefs?.contains("${context.packageName}/${service.canonicalName}") == true) return true
            return false
        }

        private const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
        private const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"

        fun openAccessibility(context: Context) = with(context) {
            val bundle = Bundle()
            val componentName = ComponentName(
                BuildConfig.APPLICATION_ID,
                ClipboardAccessibilityService::class.java.name
            ).flattenToString()
            bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
                putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
                putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
            }
            startActivity(intent)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun showOverlayDialog(context: Context): AlertDialog = with(context) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Suggestions [BETA]")
                .setMessage(getString(R.string.suggestion_capture))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    openSystemOverlay(this)
                }
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun openSystemOverlay(context: Context) = with(context) {
            val myIntent =
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ).apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            startActivity(myIntent)
        }

        fun logoutFromDatabase(
            context: Context,
            appSettings: AppSettings,
            dbConnectionProvider: DBConnectionProvider
        ) {
            dbConnectionProvider.optionsProvider()?.apply {
                if (isAuthNeeded) {
                    FirebaseSyncHelper.get()?.let { app ->
                        Firebase.auth(app).signOut()
                    }
                    AuthenticationHelper.signOutGoogle(context, authClientId)
                }
            }
            dbConnectionProvider.detachDataFromAll()

            appSettings.setDatabaseDeleteBindingEnabled(false)
            appSettings.setDatabaseBindingEnabled(false)
            appSettings.setDatabaseAutoSyncEnabled(false)
        }

        fun loginToDatabase(
            appSettings: AppSettings,
            dbConnectionProvider: DBConnectionProvider,
            options: FBOptions
        ) {
            dbConnectionProvider.saveOptionsToAll(options)

            appSettings.setDatabaseBindingEnabled(true)
            appSettings.setDatabaseAutoSyncEnabled(true)
        }
    }
}