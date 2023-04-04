package com.kpstv.xclipper.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity
import com.github.omadahealth.lollipin.lib.managers.LockManager
import com.kpstv.xclipper.PinLockHelper
import com.kpstv.xclipper.PinLockHelper.Result.PIN_LOCK_RESULT_SUCCESS
import com.kpstv.xclipper.core_pinlock.R
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import es.dmoral.toasty.Toasty

internal class CustomPinLockActivity : AppLockActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppThemeHelper.applyActivityTheme(this)
        super.onCreate(savedInstanceState)
    }

    companion object {
        private const val VERIFICATION_ENABLED = "verification_enabled"
        private const val DISABLING_PIN_LOCK = "disabling_pin_lock"
        private const val BACK_PRESS_ENABLED = "back_press_enabled"

        fun createIntentForLockVerification(context: Context) : Intent {
            return Intent(context, CustomPinLockActivity::class.java).apply {
                action = VERIFICATION_ENABLED
            }
        }
        fun createIntentForCreatingPinLock(context: Context) : Intent {
            return Intent(context, CustomPinLockActivity::class.java).apply {
                action = BACK_PRESS_ENABLED
                putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
            }
        }
        fun createIntentForDisablingPinLock(context: Context) : Intent {
            return Intent(context, CustomPinLockActivity::class.java).apply {
                action = DISABLING_PIN_LOCK
            }
        }
    }

    override fun showForgotDialog() { }

    override fun onPinFailure(attempts: Int) {
        if (attempts == 4) {
            Toasty.error(this, getString(R.string.pin_lock_attempt_reach)).show()
            onBackPressed()
        }
        PinLockHelper.updateListenerStatus(PinLockHelper.Status.Failed)
    }

    override fun onPinSuccess(attempts: Int) {
        if (mType != AppLock.CHANGE_PIN) {
            setResult(PIN_LOCK_RESULT_SUCCESS)
        }
        if (intent.action == DISABLING_PIN_LOCK) {
            val lockManager = LockManager.getInstance()
            lockManager.appLock.setPasscode(null)
            Toasty.info(this, getString(R.string.pin_lock_disabled)).show()
        }
        PinLockHelper.updateListenerStatus(PinLockHelper.Status.Success)
    }

    override fun onBackPressed() {
        when (intent.action) {
            DISABLING_PIN_LOCK, BACK_PRESS_ENABLED -> finish()
            VERIFICATION_ENABLED -> finishAffinity()
            else -> super.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()

        //Animate if greater than 2.3.3
        overridePendingTransition(R.anim.nothing, R.anim.slide_down)
    }
}