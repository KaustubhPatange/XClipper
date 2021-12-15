package com.kpstv.xclipper.di.feature_onboarding

import com.kpstv.onboarding.di.navigation.OnBoardingNavigation
import com.kpstv.xclipper.di.feature_onboarding.navigation.OnBoardingNavigationImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@[Module InstallIn(ActivityComponent::class)]
abstract class NavigationModule {
    @Binds
    abstract fun onBoardingNavigation(onBoardingNavigationImpl: OnBoardingNavigationImpl) : OnBoardingNavigation
}