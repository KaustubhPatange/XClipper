package com.kpstv.xclipper.di.feature_suggestions.action

import android.content.Context
import com.kpstv.xclipper.di.action.ClipboardAccessibilityServiceActions
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ClipboardAccessibilityServiceActionsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ClipboardAccessibilityServiceActions {
    override fun sendBubbleExpandedState(isExpanded: Boolean) {
        ClipboardAccessibilityService.Actions.sendExpandedStateStatus(
            context = context,
            isExpanded = isExpanded
        )
    }
    override fun sendClipboardInsertText(wordLength: Int, text: String) {
        ClipboardAccessibilityService.Actions.sendClipboardInsertText(
            context = context,
            wordLength = wordLength,
            text = text
        )
    }
}