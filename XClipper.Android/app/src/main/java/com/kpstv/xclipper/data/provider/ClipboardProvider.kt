package com.kpstv.xclipper.data.provider

import android.content.ClipData
import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.model.Clip

interface ClipboardProvider {
    fun startObserving()
    fun observeClipboardChange()
    fun setClipboard(item: ClipData?)
    fun getClipboard(): ClipData?
    fun stopObserving()
    fun ignoreChange(block: () -> Unit)
    fun setCurrentClip(text: String)
    fun getCurrentClip() : LiveData<String>
}