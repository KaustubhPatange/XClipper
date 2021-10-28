package com.kpstv.xclipper.service.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.kpstv.xclipper.extensions.launchInMain
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

interface ClipboardLogDetector {

    interface Listener {
        fun onClipboardEventDetected()
        fun onPermissionNotGranted()
    }

    fun registerListener(listener: Listener)
    fun startDetecting()
    fun stopDetecting()
    fun isStarted() : Boolean
    fun dispose()

    companion object {
        fun newInstanceCompat(context: Context) : ClipboardLogDetector {
            return if (Build.VERSION.SDK_INT >= 29)
                ClipboardLogDetector29Impl(context)
            else
                ClipboardLogDetector21Impl()
        }
        fun isDetectionVersionCompatible(context: Context) : Boolean {
            return Build.VERSION.SDK_INT >= 29
        }
        fun isDetectionCompatible(context: Context) : Boolean {
            return if (Build.VERSION.SDK_INT >= 29)
                context.checkSelfPermission(Manifest.permission.READ_LOGS) == PackageManager.PERMISSION_GRANTED && Settings.canDrawOverlays(context)
            else
                false
        }
    }
}

@RequiresApi(29)
private class ClipboardLogDetector29Impl(private val context: Context) : ClipboardLogDetector {
    private var job: CompletableJob = SupervisorJob()
    private var listener: ClipboardLogDetector.Listener? = null
    private var currentTime: Long = 0
    private var hasStarted: Boolean = false
    private val logcatFlow = flow {
        Runtime.getRuntime().exec("logcat -c").waitFor()
        Runtime.getRuntime().exec("logcat e -T 1")
            .inputStream
            .bufferedReader()
            .useLines { lines -> lines.forEach { line -> emit(line) } }
    }

    override fun isStarted(): Boolean {
        return hasStarted
    }

    override fun registerListener(listener: ClipboardLogDetector.Listener) {
        this.listener = listener
    }

    override fun startDetecting() {
        if (!isRequiredPermissionGranted()) {
            listener?.onPermissionNotGranted()
            return
        }
        hasStarted = true
        val coroutineContext = createAndSetNewScope()
        CoroutineScope(coroutineContext).launch {
            logcatFlow.collect { line ->
                if (line.contains("Denying") && line.contains("clipboard") && line.contains(context.packageName)) {
                    val timeMillis = System.currentTimeMillis()
                    if ((timeMillis - currentTime) > 500) {
                        currentTime = timeMillis
                        launchInMain { listener?.onClipboardEventDetected() }
                    }
                }
            }
            println("Cancelled")
        }
    }

    override fun stopDetecting() {
        hasStarted = false
        if (job.isActive) job.cancel()
    }

    override fun dispose() {
        listener = null
        stopDetecting()
    }

    private fun createAndSetNewScope() : CoroutineContext {
        if (job.isActive) job.cancel()
        job = SupervisorJob()
        return Dispatchers.IO + job
    }

    private fun isRequiredPermissionGranted() : Boolean {
        return ClipboardLogDetector.isDetectionCompatible(context)
    }
}

private class ClipboardLogDetector21Impl : ClipboardLogDetector {
    override fun dispose() { /* no-op */ }
    override fun registerListener(listener: ClipboardLogDetector.Listener) { /* no-op */ }
    override fun startDetecting() { /* no-op */ }
    override fun stopDetecting() { /* no-op */ }
    override fun isStarted(): Boolean = true
}