package com.kpstv.xclipper.ui.helpers

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.getFormattedDate
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.dialogs.CustomLottieDialog
import com.kpstv.xclipper.ui.fragments.Home
import java.util.*

@Deprecated("Sync button will let user know about synchronization feature")
class SyncDialogHelper(
    private val activity: FragmentActivity,
    private val preferenceProvider: PreferenceProvider,
    private val dbConnectionProvider: DBConnectionProvider
) : AbstractFragmentHelper<Home>(activity, Home::class) {

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
            CustomLottieDialog(this)
                .setLottieView(R.raw.connection)
                .setLoop(false)
                .setSpeed(3.5f)
                .setTitle(R.string.synchronize)
                .setMessage(R.string.synchronize_text)
                .setNeutralButton(R.string.later)
                .setPositiveButton(R.string.learn_more) {
                    Utils.commonUrlLaunch(this, getString(R.string.app_docs_sync))
                }
                .show()
        }
    }
}