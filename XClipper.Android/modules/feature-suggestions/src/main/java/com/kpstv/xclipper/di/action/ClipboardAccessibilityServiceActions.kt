package com.kpstv.xclipper.di.action

interface ClipboardAccessibilityServiceActions {
    fun sendClipboardInsertText(wordLength: Int, text: String)
}