package com.kpstv.xclipper.di.feature_home.navigation

import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.xclipper.di.navigation.SettingsNavigation
import com.kpstv.xclipper.ui.activities.NavViewModel
import com.kpstv.xclipper.ui.activities.Start
import javax.inject.Inject

class SettingsNavigationImpl @Inject constructor(
    private val activity: FragmentActivity
) : SettingsNavigation {
    override fun navigate() {
        (activity as Start).viewModels<NavViewModel>().value.navigateTo(
            screen = Start.Screen.SETTING,
            animation = AnimationDefinition.Fade,
            transactionType = FragmentNavigator.TransactionType.ADD,
            remember = true,
        )
    }
}