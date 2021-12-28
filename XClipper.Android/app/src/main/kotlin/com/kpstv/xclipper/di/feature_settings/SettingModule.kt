package com.kpstv.xclipper.di.feature_settings

import com.kpstv.xclipper.di.feature_settings.fragments.ActionSettingFragmentImpl
import com.kpstv.xclipper.di.feature_settings.improve_detection.ImproveDetectionQuickTipImpl
import com.kpstv.xclipper.di.fragments.ActionSettingFragment
import com.kpstv.xclipper.di.improve_detection.ImproveDetectionQuickTip
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class SettingModule {
    @Binds
    @Singleton
    abstract fun actionSettingFragment(actionSettingFragmentImpl: ActionSettingFragmentImpl) : ActionSettingFragment

    @Binds
    @Singleton
    abstract fun improveDetectionQuickTip(improveDetectionQuickTipImpl: ImproveDetectionQuickTipImpl) : ImproveDetectionQuickTip
}