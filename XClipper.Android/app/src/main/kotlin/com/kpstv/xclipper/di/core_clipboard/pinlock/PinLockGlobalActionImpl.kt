package com.kpstv.xclipper.di.core_clipboard.pinlock

import android.content.Context
import com.kpstv.xclipper.di.pinlock.PinLockGlobalAction
import com.kpstv.xclipper.service.receiver.PinGrantScreenLockReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PinLockGlobalActionImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PinLockGlobalAction {

    override fun onScreenLocked() {
        val intent = PinGrantScreenLockReceiver.createOnScreenLockedEvent(context)
        context.sendBroadcast(intent)
    }
}