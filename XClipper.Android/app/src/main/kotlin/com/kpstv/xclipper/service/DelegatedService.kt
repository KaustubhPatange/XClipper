package com.kpstv.xclipper.service

import android.content.ComponentCallbacks2

interface ServiceInterface {
    val deviceRunningLowMemory : Boolean get() = false
    fun onTrimMemoryLevel(level: Int)
    // Call before checking whether device running on low memory.
    fun updateMemory()
}

class ServiceInterfaceImpl : ServiceInterface {
    @Volatile
    private var onLowMemory = false

    override val deviceRunningLowMemory = onLowMemory
    private var currentLowMemory: Long = -1L
    override fun onTrimMemoryLevel(level: Int) {
        val freeMemory = Runtime.getRuntime().freeMemory()
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            // we are on low memory, start monitoring
            currentLowMemory = freeMemory
        }

        if (freeMemory <= currentLowMemory) {
            onLowMemory = true
        }
    }

    override fun updateMemory() {
        if (currentLowMemory == -1L) return
        val freeMemory = Runtime.getRuntime().freeMemory()
        if (freeMemory > currentLowMemory) {
            onLowMemory = false
        }
    }
}