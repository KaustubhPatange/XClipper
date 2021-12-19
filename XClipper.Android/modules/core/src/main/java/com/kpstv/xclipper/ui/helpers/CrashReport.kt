package com.kpstv.xclipper.ui.helpers

import android.content.Context
import com.kpstv.core.BuildConfig
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.extensions.utils.SystemUtils
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel

object CrashReport {

    fun sendFatalException(context: Context?, throwable: Throwable) {
        Sentry.captureEvent(SentryEvent(throwable).apply {
            level = SentryLevel.FATAL
            if (context != null) applyDefaults(context)
        })
    }

    fun sendNonFatalException(context: Context?, throwable: Throwable) {
        Sentry.captureEvent(SentryEvent(throwable).apply {
            level = SentryLevel.WARNING
            if (context != null) applyDefaults(context)
        })
    }

    private fun SentryEvent.applyDefaults(context: Context) {
        val entryPoints = CommonReusableEntryPoints.get(context.applicationContext)
        val appSettings = entryPoints.appSettings()
        val accessibilityService = entryPoints.clipboardServiceHelper()

        environment = if (BuildConfig.DEBUG) "staging" else "production"
        setExtra("Device Id", SystemUtils.getDeviceId(context))
        setExtra("System Overlay Enabled", SystemUtils.isSystemOverlayEnabled(context))
        setExtra("Improve Detection Enabled", appSettings.isImproveDetectionEnabled())
        setExtra("Clipboard Suggestion Enabled", appSettings.canShowClipboardSuggestions())
        setExtra("Database Binding (sync) Enabled", appSettings.isDatabaseBindingEnabled())
        setExtra("Clipboard Accessibility Service Enabled", accessibilityService)
    }
}