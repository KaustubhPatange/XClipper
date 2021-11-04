package com.kpstv.xclipper.ui.helpers

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.kpstv.xclipper.ui.activities.Start
import kotlinx.coroutines.launch

class ActivityIntentHelper(private val activity: Start) {

    /**
     * Handle intent actions & deep-links for the parent activity.
     *
     * @return True if the intent is handled.
     */
    fun handle(intent: Intent?): Boolean {
        if (intent == null) return false
//        val data: Uri? = intent.data
        if (intent.action == ACTION_FORCE_CHECK_UPDATE) {
            activity.lifecycleScope.launch {
                activity.updateHelper.checkForUpdatesFromGithub()
            }
            return true
        }

        return false
    }

    companion object {
        const val ACTION_FORCE_CHECK_UPDATE = "action_force_check_update"
    }
}
