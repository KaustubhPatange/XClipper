package com.kpstv.xclipper.data.provider

import android.content.ClipData
import android.net.Uri
import androidx.lifecycle.LiveData
import com.kpstv.xclipper.extensions.SimpleFunction
import android.content.ClipboardManager

interface ClipboardProvider {
    fun isObserving(): Boolean
    fun startObserving()

    /**
     * @param action perform action with the data.
     */
    fun observeClipboardChange(action: (data: String) -> Boolean)
    fun removeClipboardObserver()
    fun setClipboard(data: String?, flag: ClipboardProviderFlags = ClipboardProviderFlags.None)
    fun setClipboard(uri: Uri, flag: ClipboardProviderFlags = ClipboardProviderFlags.None)
    fun getClipboard(): ClipData?
    fun clearClipboard()
    fun stopObserving()
    fun ignoreChange(block: SimpleFunction)
    fun setCurrentClip(text: String)
    fun getCurrentClip(): LiveData<String>
}

enum class ClipboardProviderFlags(val label: String?) {
    /**
     * Does not fire the action set through [ClipboardProvider.observeClipboardChange].
     */
    IgnoreObservedAction("com.kpstv.xclipper:clip_provider:ignore_action"),

    /**
     * Ignore listener event [ClipboardManager.OnPrimaryClipChangedListener.onPrimaryClipChanged].
     */
    IgnorePrimaryChangeListener("com.kpstv.xclipper:clip_provider:ignore_listener"),
    None(null)
}