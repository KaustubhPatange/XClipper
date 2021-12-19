package com.kpstv.xclipper.data.provider

import android.os.Build
import androidx.annotation.RequiresApi

interface ClipboardServiceHelper {
    fun isRunning() : Boolean

    @RequiresApi(Build.VERSION_CODES.N)
    fun stopService()
}