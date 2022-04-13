package com.kpstv.xclipper.di.action

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.kpstv.xclipper.data.model.Clip
import kotlinx.parcelize.Parcelize

interface SpecialActionsLauncher {
    fun launch(data: String, option: SpecialActionOption = SpecialActionOption())
    fun launch(parentFragment: Fragment, clip: Clip, option: SpecialActionOption = SpecialActionOption())
}

@Parcelize
data class SpecialActionOption(val showShareOption: Boolean = true) : Parcelable