package com.kpstv.xclipper.ui.dialogs

import android.content.Context
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.setPadding
import com.kpstv.xclipper.extensions.utils.ClipboardUtils
import com.kpstv.xclipper.extensions.utils.SystemUtils
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.ui.utils.LaunchUtils

object Dialogs {

    /* Pin lock info dialog */

    fun showPinLockInfoDialog(context: Context, onPositive: () -> Unit) {
        CustomLottieDialog(context)
            .setTitle(R.string.pin_lock_dialog_title)
            .setMessage(R.string.pin_lock_dialog_text)
            .setLottieRes(R.raw.fingerprint)
            .setLoop(false)
            .setLottieResCredits(context.getString(R.string.fingerprint_author))
            .setPositiveButton(R.string.pin_lock_dialog_positive, onPositive)
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    /* Improve detection dialog */

    fun showImproveDetectionDialog(context: Context, additional: CustomLottieDialog.() -> Unit = {}) {
        CustomLottieDialog(context)
            .setTitle(R.string.adb_mode_title)
            .setMessage(R.string.adb_mode_long_summary)
            .setLottieRes(R.raw.abstract_star)
            .setLottieResCredits(context.getString(R.string.abstract_star_author))
            .setLoop(true)
            .setPositiveButton(R.string.enable) { improveDetectionAdbDialog(context) }
            .setNegativeButton(R.string.cancel, null)
            .apply(additional)
            .show()
    }
    private fun improveDetectionAdbDialog(context: Context) {
        val color = context.getColorAttr(R.attr.colorSeparator)

        val spannableString = SpannableStringBuilder()
        spannableString.append(context.getString(R.string.adb_dialog_message1))
        spannableString.append("\n\n")
        spannableString.append(context.getString(R.string.adb_dialog_message2, context.packageName), BackgroundColorSpan(color), Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val paddingHorizontally = (25 * context.resources.displayMetrics.density).toInt()
        val paddingVertically = (15 * context.resources.displayMetrics.density).toInt()
        val textView = TextView(context).apply {
            text = spannableString
            setPadding(paddingHorizontally, paddingVertically)
        }
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.adb_dialog_title)
            .setView(textView)
            .setPositiveButton(R.string.learn_more) { _, _ ->
                LaunchUtils.commonUrlLaunch(context, context.getString(R.string.app_docs_improve_detect))
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

}