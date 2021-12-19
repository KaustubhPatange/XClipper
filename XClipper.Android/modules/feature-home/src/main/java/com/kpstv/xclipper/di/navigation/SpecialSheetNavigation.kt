package com.kpstv.xclipper.di.navigation

import androidx.fragment.app.Fragment
import com.kpstv.xclipper.data.model.Clip

interface SpecialSheetNavigation {
    fun navigate(parentFragment: Fragment, clip: Clip)
}