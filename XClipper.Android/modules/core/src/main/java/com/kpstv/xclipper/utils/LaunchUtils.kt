package com.kpstv.xclipper.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kpstv.core.R
import es.dmoral.toasty.Toasty

object LaunchUtils {
    fun commonUrlLaunch(context: Context, url: String): Unit = with(context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        }catch (e: Exception) {
            Toasty.error(this, R.string.err_action_web).show()
        }
    }
}