package com.kpstv.xclipper.ui.helpers.fragments

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extension.getFormattedDate
import com.kpstv.xclipper.feature_home.R
import com.kpstv.xclipper.ui.dialogs.CustomLottieDialog
import com.kpstv.xclipper.ui.fragments.Home
import com.kpstv.xclipper.ui.helpers.AbstractFragmentHelper
import com.kpstv.xclipper.ui.utils.LaunchUtils
import java.util.*

class SyncDialogHelper(
    private val activity: FragmentActivity,
) : AbstractFragmentHelper<Home>(activity, Home::class) {

    private val preferenceProvider : PreferenceProvider = hiltCommonEntryPoints.preferenceProvider()
    private val dbConnectionProvider : DBConnectionProvider = hiltCommonEntryPoints.dbConnectionProvider()

    override fun onFragmentViewCreated() {
        attach()
    }

    private fun attach() {
        if (preferenceProvider.getBooleanKey(SYNC_DO_NOT_SHOW, false)) return
        if (dbConnectionProvider.isValidData()) return
        val dateString = preferenceProvider.getStringKey(SYNC_DATE_STRING, null)
        if (dateString.isNullOrEmpty()) {
            updateDate()
            return
        }
        val oldDate = dateString.toLong()
        val newDate = Calendar.getInstance().time.getFormattedDate().toLong()
        if (newDate >= oldDate) {
            updateDate()
            showDialog(activity)
        }
    }

    private fun updateDate() {
        val dateAfter2Days = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_WEEK, 2)
        }.time.getFormattedDate()
        preferenceProvider.putStringKey(SYNC_DATE_STRING, dateAfter2Days)
    }

    companion object {
        private const val SYNC_DO_NOT_SHOW = "sync_do_not_show"
        private const val SYNC_DATE_STRING = "sync_date_string"

        fun showDialog(context: Context) = with(context) {
            CustomLottieDialog(context)
                .setLottieRes(R.raw.connection)
                .setLoop(false)
                .setSpeed(3.5f)
                .setTitle(R.string.synchronize)
                .setMessage(R.string.synchronize_text)
                .setNeutralButton(R.string.later)
                .setPositiveButton(R.string.learn_more) {
                    LaunchUtils.commonUrlLaunch(this, getString(R.string.app_docs_sync))
                }
                .show()
        }
    }
}