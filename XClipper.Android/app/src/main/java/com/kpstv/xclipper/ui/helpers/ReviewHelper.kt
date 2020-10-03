package com.kpstv.xclipper.ui.helpers

import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.review.testing.FakeReviewManager
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.ui.fragments.Home

class ReviewHelper(
    private val activity: FragmentActivity
): AbstractFragmentHelper<Home>(activity, Home::class) {
    private val manager = FakeReviewManager(activity)

    override fun onFragmentViewCreated() {
        attach()
    }

    private fun attach(): Unit = with(activity) {
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