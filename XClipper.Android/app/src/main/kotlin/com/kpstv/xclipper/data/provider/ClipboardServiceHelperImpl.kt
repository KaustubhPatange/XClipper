package com.kpstv.xclipper.data.provider

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ClipboardServiceHelperImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ClipboardServiceHelper {
    override fun isRunning(): Boolean = ClipboardAccessibilityService.isRunning(context)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun stopService() {
        ClipboardAccessibilityService.disableService(context)
    }
}