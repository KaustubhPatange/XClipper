package com.kpstv.xclipper.extensions.utils

import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat
import com.kpstv.core.R

object ShareUtils {
    fun shareText(context: Context, text: String) {
        val intent = ShareCompat.IntentBuilder(context)
            .setChooserTitle(context.getString(R.string.share))
            .setType("text/plain")
            .setText(text)
            .intent
        val shareIntent = Intent.createChooser(intent, null)
        context.startActivity(shareIntent)
    }
}