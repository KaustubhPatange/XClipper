package com.kpstv.xclipper.ui.helpers.fragments

import androidx.fragment.app.FragmentActivity
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.ui.activities.NavViewModel
import com.kpstv.xclipper.ui.activities.Start
import com.kpstv.xclipper.ui.fragments.Home
import com.kpstv.xclipper.ui.helpers.AbstractFragmentHelper

class DisclosureHelper (
    private val activity: FragmentActivity,
    private val navViewModel: NavViewModel
) : AbstractFragmentHelper<Home>(activity, Home::class) {

    override fun onFragmentViewCreated() {
        attach()
    }

    private fun attach() {
        val shouldShow = !CommonReusableEntryPoints.get(activity.applicationContext).appSettings().isDisclosureAgreementShown()
        if (shouldShow) {
            navViewModel.navigateTo(Start.Screen.DISCLOSURE)
        }
    }
}