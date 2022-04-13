package com.kpstv.xclipper.di.feature_home

import com.kpstv.xclipper.di.feature_home.navigation.SettingsNavigationImpl
import com.kpstv.xclipper.di.navigation.SettingsNavigation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class HomeModule {
    @Binds
    abstract fun settingsNavigation(settingsNavigationImpl: SettingsNavigationImpl) : SettingsNavigation
}