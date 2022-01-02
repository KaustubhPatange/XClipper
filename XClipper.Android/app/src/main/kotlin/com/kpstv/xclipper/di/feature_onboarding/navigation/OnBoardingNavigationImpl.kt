package com.kpstv.xclipper.di.feature_onboarding.navigation

import androidx.fragment.app.FragmentActivity
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.HistoryOptions
import com.kpstv.onboarding.di.navigation.OnBoardingNavigation
import com.kpstv.xclipper.ui.activities.Start
import javax.inject.Inject

class OnBoardingNavigationImpl @Inject constructor(
    private val startActivity: FragmentActivity
) : OnBoardingNavigation {
    override fun goToNext() {
        val navOptions = FragmentNavigator.NavOptions(
            remember = false,
            animation = AnimationDefinition.Fade,
            historyOptions = HistoryOptions.ClearHistory
        )
        (startActivity as FragmentNavigator.Transmitter).getNavigator().navigateTo(
            clazz = Start.Screen.HOME.clazz,
            navOptions = navOptions
        )
    }
}