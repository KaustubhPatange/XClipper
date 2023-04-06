package com.kpstv.xclipper.ui.helpers

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kpstv.xclipper.PinLockHelper
import com.kpstv.xclipper.ui.activities.Start

class ActivityPinLockHelper(private val activity: Start) {
    private var isRunning = false
    private var init = false

    private var lastUnlock: Long = -1L

    init {
        PinLockHelper.addPinLockStatusListener(activity) { status ->
            if (status == PinLockHelper.Status.Success) {
                init = true
                isRunning = false
                lastUnlock = System.currentTimeMillis()
            }
        }

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (!isRunning && !init) {
                    init()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                if (init) {
                    init = false
                }
            }
        })
    }

    fun init() {
        if (lastUnlock == -1L || System.currentTimeMillis() - lastUnlock >= 60 * 1000) {
            isRunning = true
            PinLockHelper.checkPinLock(activity)
        }
    }
}