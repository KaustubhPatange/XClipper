package com.kpstv.xclipper.extensions.utils

object SyncUtils {
    private const val FB_MIN_ITEM_STORAGE = 5
    private const val FB_MAX_ITEM_STORAGE = 20
    private const val FB_MIN_DEVICE_CONNECTION = 1
    private const val FB_MAX_DEVICE_CONNECTION = 5

    fun getMaxConnection(isLicensed: Boolean): Int =
        if (isLicensed) FB_MAX_DEVICE_CONNECTION else FB_MIN_DEVICE_CONNECTION

    fun getMaxStorage(isLicensed: Boolean): Int =
        if (isLicensed) FB_MAX_ITEM_STORAGE else FB_MIN_ITEM_STORAGE
}