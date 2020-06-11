package com.kpstv.xclipper.data.provider

import android.content.ClipData
import com.kpstv.xclipper.data.model.Clip

interface ClipboardProvider {
    fun startObserving()
    fun observeClipboardChange()
    fun setClipboard(item: ClipData?)
    fun getClipboard(): ClipData?
    fun stopObserving()
    fun ignoreChange(block: () -> Unit)
}