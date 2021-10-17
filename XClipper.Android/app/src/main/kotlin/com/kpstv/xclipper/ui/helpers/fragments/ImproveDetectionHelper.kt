package com.kpstv.xclipper.ui.helpers.fragments

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.service.helper.ClipboardLogDetector
import com.kpstv.xclipper.ui.dialogs.Dialogs
import com.kpstv.xclipper.ui.fragments.Home
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.*

class ImproveDetectionHelper(
    private val activity: FragmentActivity,
) : AbstractFragmentHelper<Home>(activity, Home::class) {

    private val preferenceProvider : PreferenceProvider = hiltCommonEntryPoints.preferenceProvider()
    private val appSettings : AppSettings = hiltCommonEntryPoints.appSettings()

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
            val shouldShowTip = canShowQuickTip(activity, preferenceProvider, appSettings)
            if (newDate >= oldDate && shouldShowTip) {
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
            doOnAction: SimpleFunction
        ) {
            val context = container.context

            val entryPoints = CommonReusableEntryPoints.get(context)
            val preferenceProvider = entryPoints.preferenceProvider()
            val appSettings = entryPoints.appSettings()

            val showQuickTip = canShowQuickTip(context, preferenceProvider, appSettings)

            if (showQuickTip) {
                val tipView = QuickTip(container).run {
                    setTitleText(R.string.adb_mode_title)
                    setSubText(R.string.adb_mode_summary)
                    setIcon(R.drawable.ic_increase)
                    applyColor(context.colorFrom(R.color.palette_android))
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

        private fun canShowQuickTip(context: Context, preferenceProvider: PreferenceProvider, appSettings: AppSettings) : Boolean {
            return !preferenceProvider.getBooleanKey(QUICK_TIP_SHOWN, false) &&
                    ClipboardAccessibilityService.isRunning(context) &&
                    ClipboardLogDetector.isDetectionVersionCompatible(context) &&
                    if (ClipboardLogDetector.isDetectionCompatible(context)) { !appSettings.isImproveDetectionEnabled() } else true
        }
    }
}