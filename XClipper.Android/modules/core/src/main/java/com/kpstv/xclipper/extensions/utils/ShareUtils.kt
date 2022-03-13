package com.kpstv.xclipper.extensions.utils

import android.app.Activity
import android.content.Intent
import androidx.core.app.ShareCompat
import com.kpstv.core.R
import com.kpstv.xclipper.data.model.Clip

object ShareUtils {
    fun shareText(context: Activity, clip: Clip) {
        val intent = ShareCompat.IntentBuilder(context)
            .setChooserTitle(context.getString(R.string.share))
            .setType("text/plain")
            .setText(clip.data)
            .intent
        val shareIntent = Intent.createChooser(intent, null)
        context.startActivity(shareIntent)
    }
}