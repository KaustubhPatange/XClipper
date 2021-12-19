package com.kpstv.xclipper.extensions.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf

object SystemUtils {
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    @RequiresApi(Build.VERSION_CODES.M)
    fun openSystemOverlaySettings(context: Context) = with(context) {
        val myIntent =
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        startActivity(myIntent)
    }

    fun isSystemOverlayEnabled(context: Context): Boolean = with(context) {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
    private const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"

    fun openAccessibilitySettings(context: Context, componentName: ComponentName? = null) = with(context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (componentName != null) {
                val finalizedComponentName = componentName.flattenToString()
                putExtra(EXTRA_FRAGMENT_ARG_KEY, finalizedComponentName)
                putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundleOf(EXTRA_FRAGMENT_ARG_KEY to finalizedComponentName))
            }
        }
        startActivity(intent)
    }

    // TODO: Make this work for all accessibility service & not limited to app's accessibility services.
    fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService?>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (enabledService in enabledServices) {
            val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name)
                return true
        }
        val accessibilityPrefs = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (accessibilityPrefs?.contains("${context.packageName}/${service.canonicalName}") == true) return true
        return false
    }

    fun isServiceRunning(context: Context, service: Class<out Service>): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.getRunningServices(Int.MAX_VALUE).any { it.service.className == service.canonicalName }
    }
}