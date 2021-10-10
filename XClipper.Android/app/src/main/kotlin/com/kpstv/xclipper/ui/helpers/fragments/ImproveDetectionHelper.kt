package com.kpstv.xclipper.ui.helpers.fragments

import android.os.Build
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.service.helper.ClipboardLogDetector
import com.kpstv.xclipper.ui.dialogs.Dialogs
import com.kpstv.xclipper.ui.fragments.Home
import java.util.*

class ImproveDetectionHelper(
    private val activity: FragmentActivity,
    private val preferenceProvider: PreferenceProvider
) : AbstractFragmentHelper<Home>(activity, Home::class) {

    override fun onFragmentViewCreated() {
        attach()
    }

    private fun attach() {
        val showDialog = preferenceProvider.getBooleanKey(SHOW_DIALOG, true)
        if (showDialog) {
            val dateString = preferenceProvider.getStringKey(DATE_DATA, null)
            if (dateString.isNullOrEmpty()) {
                updateDate()
                return
            }
            val oldDate = dateString.toLong()
            val newDate = Calendar.getInstance().time.getFormattedDate().toLong()
            if (newDate >= oldDate) {
                updateDate()
                Dialogs.showImproveDetectionDialog(activity) {
                    setNeutralButton(R.string.do_not_show) {
                        preferenceProvider.putBooleanKey(SHOW_DIALOG, false)
                    }
                }
            }
        }
    }

    private fun updateDate() {
        val dateAfter1Days = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_WEEK, 1)
        }.time.getFormattedDate()
        preferenceProvider.putStringKey(DATE_DATA, dateAfter1Days)
    }

    companion object {
        private const val QUICK_TIP_SHOWN = "improve_quick_tip_shown"
        private const val SHOW_DIALOG = "improve_show_dialog"
        private const val DATE_DATA = "improve_date_data"

        fun addQuickTip(
            container: ViewGroup,
            preferenceProvider: PreferenceProvider,
            doOnAction: SimpleFunction
        ) {
            val showQuickTip = !preferenceProvider.getBooleanKey(QUICK_TIP_SHOWN, false) xnor
                    ClipboardAccessibilityService.isRunning(container.context) xnor
                    ClipboardLogDetector.isDetectionVersionCompatible(container.context) xnor
                    !ClipboardLogDetector.isDetectionCompatible(container.context)

            if (showQuickTip) {
                val tipView = QuickTip(container).run {
                    setTitleText(R.string.adb_mode_title)
                    setSubText(R.string.adb_mode_summary)
                    setIcon(R.drawable.ic_increase)
                    applyColor(container.context.colorFrom(R.color.palette_android))
                    hideButtonPanel()
                    setOnClick(doOnAction)
                    setOnLongClick {
                        preferenceProvider.putBooleanKey(QUICK_TIP_SHOWN, true)
						dismiss()
                    }
                    updatePadding(top = 7.dp())
                    create()
                }
                container.addView(tipView)
            }
        }
    }
}