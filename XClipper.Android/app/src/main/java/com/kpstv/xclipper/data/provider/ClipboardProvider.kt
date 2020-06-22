package com.kpstv.xclipper.data.provider

import android.content.ClipData
import androidx.lifecycle.LiveData
import com.kpstv.xclipper.extensions.SimpleFunction

interface ClipboardProvider {
    fun startObserving()
    fun observeClipboardChange()
    fun setClipboard(item: ClipData?)
    fun getClipboard(): ClipData?
    fun stopObserving()
    fun ignoreChange(block: SimpleFunction)
    fun setCurrentClip(text: String)
    fun getCurrentClip(): LiveData<String>
}