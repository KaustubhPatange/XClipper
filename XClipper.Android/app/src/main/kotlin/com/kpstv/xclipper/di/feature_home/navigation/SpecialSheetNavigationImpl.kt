package com.kpstv.xclipper.di.feature_home.navigation

import androidx.fragment.app.Fragment
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.di.navigation.SpecialSheetNavigation
import com.kpstv.xclipper.ui.fragments.sheets.SpecialBottomSheet
import javax.inject.Inject

class SpecialSheetNavigationImpl @Inject constructor() : SpecialSheetNavigation {
    override fun navigate(parentFragment: Fragment, clip: Clip) {
        SpecialBottomSheet.show(
            fragment = parentFragment,
            clip = clip
        )
    }
}