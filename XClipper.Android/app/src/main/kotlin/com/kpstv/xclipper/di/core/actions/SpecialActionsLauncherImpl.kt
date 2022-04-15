package com.kpstv.xclipper.di.core.actions

import android.content.Context
import androidx.fragment.app.Fragment
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.di.action.SpecialActionsLauncher
import com.kpstv.xclipper.di.action.SpecialActionOption
import com.kpstv.xclipper.ui.activities.SpecialActions
import com.kpstv.xclipper.ui.fragments.sheets.SpecialBottomSheet
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SpecialActionsLauncherImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SpecialActionsLauncher {

    override fun launch(data: String, option: SpecialActionOption) {
        SpecialActions.launch(
            context = context,
            clipData = data,
            option = option,
        )
    }

    override fun launch(parentFragment: Fragment, clip: Clip, option: SpecialActionOption) {
        SpecialBottomSheet.show(
            fragment = parentFragment,
            clip = clip,
            option = option,
        )
    }
}