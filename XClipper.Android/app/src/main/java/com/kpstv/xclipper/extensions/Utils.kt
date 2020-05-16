package com.kpstv.xclipper.extensions

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip

class Utils {
    companion object {
        fun isRunning(ctx: Context): Boolean {
            val activityManager =
                ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks =
                activityManager.getRunningTasks(Int.MAX_VALUE)
            for (task in tasks) {
                if ("com.kpstv.xclipper.service.ChangeClipboardActivity".equals(task.baseActivity!!.className, ignoreCase = true)
                ) return true
            }
            return false
        }

        fun shareText(context: Activity, clip: Clip) {
            val intent = ShareCompat.IntentBuilder.from(context)
                .setChooserTitle(context.getString(R.string.share))
                .setType("text/plain")
                .setText(clip.data?.Decrypt())
                .intent
            val shareIntent = Intent.createChooser(intent, null)
            context.startActivity(shareIntent)
        }

       /* @JvmStatic
        fun cafeBarToast(context: Context, message: String, buttonText: String, block: (CafeBar) -> Unit): CafeBar {
            return CafeBar.builder(context)
                .content(message)
                .floating(true)
                .duration(CafeBar.Duration.INDEFINITE)
                .neutralText(buttonText)
                .onNeutral {
                   block.invoke(it)
                }
                .autoDismiss(false)
                .showShadow(true)
                .build()
        }*/
    }
}