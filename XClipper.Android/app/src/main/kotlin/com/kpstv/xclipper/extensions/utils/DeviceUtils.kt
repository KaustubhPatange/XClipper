package com.kpstv.xclipper.extensions.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

object DeviceUtils {
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )
}