package com.kpstv.xclipper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import com.github.omadahealth.lollipin.lib.managers.LockManager
import com.kpstv.xclipper.core_pinlock.R

object PinLockHelper {
    fun isPinLockEnabled() : Boolean {
        val lockManager = LockManager.getInstance()
        return lockManager.appLock.isPasscodeSet
    }
    fun checkPinLock(context: Context) {
        internalSetPinLock(context)
        if (isPinLockEnabled()) {
            val intent = com.kpstv.xclipper.ui.activities.CustomPinLockActivity.createIntentForLockVerification(context).apply {
                if (context !is Activity) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
    fun disablePinLock(activity: ComponentActivity) = with(activity) {
        if (isPinLockEnabled()) {
            startActivity(com.kpstv.xclipper.ui.activities.CustomPinLockActivity.createIntentForDisablingPinLock(this))
        }
    }
    fun createANewPinLock(context: Context) = with(context) {
        startActivity(com.kpstv.xclipper.ui.activities.CustomPinLockActivity.createIntentForCreatingPinLock(context))
    }

    fun internalRemoveAppLock(context: Context) {
        internalSetPinLock(context)
        val lockManager = LockManager.getInstance()
        lockManager.appLock.setPasscode(null)
    }

    private fun internalSetPinLock(context: Context) {
        val lockManager = LockManager.getInstance()
        if (lockManager.appLock == null) {
            lockManager.enableAppLock(context, com.kpstv.xclipper.ui.activities.CustomPinLockActivity::class.java)
            lockManager.appLock.disable()
            lockManager.appLock.setShouldShowForgot(false)
            lockManager.appLock.logoId = R.drawable.pin_lock_ic_logo
        }
    }
}