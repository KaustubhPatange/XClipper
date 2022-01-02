package com.kpstv.xclipper.di.feature_suggestions.navigation

import android.content.Context
import com.kpstv.xcipper.di.navigation.SpecialActionsLauncher
import com.kpstv.xclipper.ui.activities.SpecialActions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SpecialActionsLauncherImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SpecialActionsLauncher {
    override fun launch(data: String) {
        SpecialActions.launch(context, data)
    }
}