package com.kpstv.xclipper.di.action

interface ClipboardAccessibilityServiceActions {
    fun sendBubbleExpandedState(isExpanded: Boolean)
    fun sendClipboardInsertText(wordLength: Int, text: String)
}