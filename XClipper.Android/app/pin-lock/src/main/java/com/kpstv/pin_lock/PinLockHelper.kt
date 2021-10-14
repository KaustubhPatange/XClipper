package com.kpstv.pin_lock

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import com.github.omadahealth.lollipin.lib.managers.LockManager

object PinLockHelper {
    fun isPinLockEnabled() : Boolean {
        val lockManager = LockManager.getInstance()
        return lockManager.appLock.isPasscodeSet
    }
    fun checkPinLock(context: Context) {
        val lockManager = LockManager.getInstance()
        lockManager.enableAppLock(context, CustomPinLockActivity::class.java)
        lockManager.appLock.disable()
        lockManager.appLock.setShouldShowForgot(false)
        lockManager.appLock.logoId = R.drawable.pin_lock_ic_logo

        if (isPinLockEnabled()) {
            val intent = CustomPinLockActivity.createIntentForLockVerification(context).apply {
                if (context !is Activity) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
    fun disablePinLock(activity: ComponentActivity) = with(activity) {
        if (isPinLockEnabled()) {
            startActivity(CustomPinLockActivity.createIntentForDisablingPinLock(this))
        }
    }
    fun createANewPinLock(context: Context) = with(context) {
        startActivity(CustomPinLockActivity.createIntentForCreatingPinLock(context))
    }
}