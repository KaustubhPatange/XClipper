package com.kpstv.xcipper.di.action

interface ClipboardAccessibilityServiceActions {
    fun sendClipboardInsertText(wordLength: Int, text: String)
}