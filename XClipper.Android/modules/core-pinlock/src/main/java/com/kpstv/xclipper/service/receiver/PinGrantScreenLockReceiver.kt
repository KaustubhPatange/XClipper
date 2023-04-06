package com.kpstv.xclipper.service.receiver

import android.content.Context
import android.content.Intent
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.extensions.elements.AbstractBroadcastReceiver
import com.kpstv.xclipper.ui.dialog.PinGrantDialog

class PinGrantScreenLockReceiver : AbstractBroadcastReceiver() {

    companion object {
        private const val SCREEN_LOCKED_ACTION = "com.kpstv.action_screen_locked"

        fun createOnScreenLockedEvent(context: Context): Intent {
            return Intent(context, PinGrantScreenLockReceiver::class.java).apply {
                action = SCREEN_LOCKED_ACTION
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            SCREEN_LOCKED_ACTION -> {
                val preferenceProvider = CommonReusableEntryPoints.get(context).preferenceProvider()
                val dialogTypeKeys = preferenceProvider.getAllKeys().filter { it.startsWith(PinGrantDialog.SAVE_TYPE) }

                dialogTypeKeys.forEach { type ->
                    val grantType = PinGrantDialog.GrantType.values()[preferenceProvider.getIntKey(type, 1)]
                    if (grantType == PinGrantDialog.GrantType.Locked) {
                        val key = type.removePrefix(PinGrantDialog.SAVE_TYPE)
                        preferenceProvider.removeKey("${PinGrantDialog.GRANT_ACCESS_MILLIS}$key")
                    }
                }
            }
        }
    }
}