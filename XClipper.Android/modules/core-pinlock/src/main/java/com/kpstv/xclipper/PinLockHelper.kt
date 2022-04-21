package com.kpstv.xclipper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.github.omadahealth.lollipin.lib.managers.LockManager
import com.kpstv.xclipper.core_pinlock.R
import com.kpstv.xclipper.ui.activities.CustomPinLockActivity

object PinLockHelper {

    object Result {
        // Will be used as set result whenever a pin attempt is success on the activity.
        internal const val PIN_LOCK_RESULT_SUCCESS = 199

        fun isSuccess(result: ActivityResult) : Boolean = result.resultCode == PIN_LOCK_RESULT_SUCCESS
    }

    fun isPinLockEnabled(context: Context) : Boolean {
        val lockManager = LockManager.getInstance()
        internalSetPinLock(context)
        return lockManager.appLock.isPasscodeSet
    }
    fun checkPinLock(context: Context) {
        checkPinLock(context, null)
    }
    internal fun checkPinLock(context: Context, activityResultLauncher: ActivityResultLauncher<Intent>? = null) {
        internalSetPinLock(context)
        if (isPinLockEnabled(context)) {
            val intent = CustomPinLockActivity.createIntentForLockVerification(context).apply {
                if (context !is Activity) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (activityResultLauncher != null) {
                activityResultLauncher.launch(intent)
            } else {
                context.startActivity(intent)
            }
        }
    }
    fun disablePinLock(activity: ComponentActivity) = with(activity) {
        if (isPinLockEnabled(this)) {
            startActivity(CustomPinLockActivity.createIntentForDisablingPinLock(this))
        }
    }
    fun createANewPinLock(context: Context) = with(context) {
        startActivity(CustomPinLockActivity.createIntentForCreatingPinLock(context))
    }

    fun internalRemoveAppLock(context: Context) {
        internalSetPinLock(context)
        val lockManager = LockManager.getInstance()
        lockManager.appLock.setPasscode(null)
    }

    private fun internalSetPinLock(context: Context) {
        val lockManager = LockManager.getInstance()
        if (lockManager.appLock == null) {
            lockManager.enableAppLock(context, CustomPinLockActivity::class.java)
            lockManager.appLock.disable()
            lockManager.appLock.setShouldShowForgot(false)
            lockManager.appLock.logoId = R.drawable.pin_lock_ic_logo
        }
    }
}