package com.kpstv.xclipper.di.feature_suggestions

import com.kpstv.xclipper.di.action.ClipboardAccessibilityServiceActions
import com.kpstv.xclipper.di.feature_suggestions.action.ClipboardAccessibilityServiceActionsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@[Module InstallIn(SingletonComponent::class)]
abstract class SuggestionsModule {
    @Binds
    abstract fun clipboardAccessibilityServiceActions(clipboardAccessibilityServiceActionsImpl: ClipboardAccessibilityServiceActionsImpl): ClipboardAccessibilityServiceActions
}