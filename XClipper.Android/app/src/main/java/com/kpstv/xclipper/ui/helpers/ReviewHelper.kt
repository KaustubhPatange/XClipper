package com.kpstv.xclipper.ui.helpers

import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.review.ReviewManagerFactory
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.ui.fragments.Home
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.util.*
import java.util.concurrent.TimeUnit

class ReviewHelper(
    private val activity: FragmentActivity
) : AbstractFragmentHelper<Home>(activity, Home::class), KodeinAware {

    override val kodein: Kodein = (activity.applicationContext as KodeinAware).kodein
    private val manager = ReviewManagerFactory.create(activity)

    private val preferenceProvider by instance<PreferenceProvider>()

    override fun onFragmentViewCreated() {
        attach()
    }

    private fun attach(): Unit = with(activity) {
        val triggerDateLong = preferenceProvider.getLongKey(SHOW_REVIEW_FLOW_PREF, -1L)

        if (triggerDateLong == -1L) {
            val setTriggerDate = Calendar.getInstance()
                .apply {add(Calendar.DAY_OF_MONTH ,2)}.time.time
            preferenceProvider.putLongKey(SHOW_REVIEW_FLOW_PREF, setTriggerDate)
            return@with
        }

        val currentDateLong = Calendar.getInstance().time.time
        if (currentDateLong >= triggerDateLong) {
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { info ->
                if (info.isSuccessful) {
                    val reviewInfo = info.result
                    val flow = manager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener {
                        // We don't know if user reviewed the app or not
                        // But we can continue the workflow
                        HVLog.d(m = "Review Dialog shown or process has already completed")
                    }
                }
            }
        }
    }

    companion object {
        private const val SHOW_REVIEW_FLOW_PREF = "show_review_flow_pref"
    }
}