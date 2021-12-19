package com.kpstv.xclipper.ui.helpers.fragments

import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.review.ReviewManagerFactory
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.ui.dialogs.CustomLottieDialog
import com.kpstv.xclipper.ui.fragments.Home
import com.kpstv.xclipper.ui.helpers.AbstractFragmentHelper
import java.util.*

class ReviewHelper(
    private val activity: FragmentActivity,
) : AbstractFragmentHelper<Home>(activity, Home::class) {

    private val preferenceProvider : PreferenceProvider = hiltCommonEntryPoints.preferenceProvider()
    private val manager = ReviewManagerFactory.create(activity)

    override fun onFragmentViewCreated() {
        attach()
    }

    /**
     * This will request a review flow from [manager]
     */
    private fun requestForReview(): Unit = with(activity) {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { info ->
            //if (info.isComplete)
            if (info.isSuccessful) {
                val reviewInfo = info.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {
                    // We don't know if user reviewed the app or not
                    // But we can continue the workflow
                    setATriggerDate() // Reset trigger date
                    HVLog.d(m = "Review Dialog shown or process has already completed")
                }
            }
        }
    }

    private fun attach(): Unit = with(activity) {
        val triggerDateLong = preferenceProvider.getLongKey(SHOW_REVIEW_FLOW_PREF, -1L)

        if (triggerDateLong == -1L) {
            setATriggerDate()
            return@with
        }

        val currentDateLong = Calendar.getInstance().time.time
        if (currentDateLong >= triggerDateLong) {
            requestForReview()
            // onNeedToShowReview.invoke(this@ReviewHelper)
        }
    }

    private fun setATriggerDate() {
        val setTriggerDate = Calendar.getInstance()
            .apply { add(Calendar.DAY_OF_MONTH, 3) }.time.time
        preferenceProvider.putLongKey(SHOW_REVIEW_FLOW_PREF, setTriggerDate)
    }

    /**
     * The class provides a default dialog that can be shown instead of custom one.
     */
    @Deprecated("Currently there is no way to detect whether a user has reviewed the app. Usage of dialogs or any similar use-cases are prohibited.")
    fun showReviewDialog() = with(activity) {
        CustomLottieDialog(this)
            .setLottieRes(R.raw.star)
            .setLoop(false)
            .setTitle(R.string.rate_title)
            .setMessage(R.string.rate_text)
            .setNeutralButton(R.string.later)
            .setPositiveButton(R.string.review) {
                requestForReview()
            }
            .show()
    }

    companion object {
        private const val SHOW_REVIEW_FLOW_PREF = "show_review_flow_pref"
    }
}