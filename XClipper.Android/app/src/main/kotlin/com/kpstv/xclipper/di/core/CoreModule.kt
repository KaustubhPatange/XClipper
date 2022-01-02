package com.kpstv.xclipper.di.core

import com.kpstv.xclipper.di.core.actions.SettingUIActionsImpl
import com.kpstv.xclipper.ui.actions.SettingUIActions
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@[Module InstallIn(SingletonComponent::class)]
abstract class CoreModule {
    @Binds
    abstract fun settingActions(settingActionsImpl: SettingUIActionsImpl) : SettingUIActions
}