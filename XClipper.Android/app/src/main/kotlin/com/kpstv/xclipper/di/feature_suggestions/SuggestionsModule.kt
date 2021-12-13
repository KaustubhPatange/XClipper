package com.kpstv.xclipper.di.feature_suggestions

import com.kpstv.xcipper.di.action.ClipboardAccessibilityServiceActions
import com.kpstv.xcipper.di.navigation.SpecialActionsLauncher
import com.kpstv.xclipper.di.feature_suggestions.action.ClipboardAccessibilityServiceActionsImpl
import com.kpstv.xclipper.di.feature_suggestions.navigation.SpecialActionsLauncherImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SuggestionsModule {
    @Binds
    abstract fun specialActionsLauncher(specialActionsLauncherImpl: SpecialActionsLauncherImpl): SpecialActionsLauncher

    @Binds
    abstract fun clipboardAccessibilityServiceActions(clipboardAccessibilityServiceActionsImpl: ClipboardAccessibilityServiceActionsImpl): ClipboardAccessibilityServiceActions
}