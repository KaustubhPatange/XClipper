package com.kpstv.xclipper.di.feature_suggestions.action

import android.content.Context
import com.kpstv.xcipper.di.action.ClipboardAccessibilityServiceActions
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ClipboardAccessibilityServiceActionsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ClipboardAccessibilityServiceActions {
    override fun sendClipboardInsertText(wordLength: Int, text: String) {
        ClipboardAccessibilityService.Actions.sendClipboardInsertText(
            context = context,
            wordLength = wordLength,
            text = text
        )
    }
}