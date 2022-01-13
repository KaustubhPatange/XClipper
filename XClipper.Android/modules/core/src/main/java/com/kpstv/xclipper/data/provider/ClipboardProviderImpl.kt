package com.kpstv.xclipper.data.provider

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.extensions.SimpleFunction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ClipboardProviderImpl @Inject constructor(@ApplicationContext private val context: Context) : ClipboardProvider {

    private val _currentClip = MutableLiveData<String>()
    private var isObserving = false
    private var isRecording = true

    private val TAG = javaClass.simpleName
    private val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    private var onPerformAction: ((String) -> Boolean)? = null

    override fun getCurrentClip() = _currentClip
    override fun setCurrentClip(text: String) {
        _currentClip.postValue(text)
    }

    override fun startObserving() {
        isRecording = true
    }

    override fun stopObserving() {
        isRecording = false
    }

    override fun ignoreChange(block: SimpleFunction) {
        stopObserving()
        block.invoke()
        startObserving()
    }

    override fun getClipboard() =
        clipboardManager.primaryClip

    override fun setClipboard(data: String?, flag: ClipboardProviderFlags) {
        val clipData = ClipData.newPlainText(flag.label, data)
        clipboardManager.setPrimaryClip(clipData)
    }

    override fun setClipboard(uri: Uri, flag: ClipboardProviderFlags) {
        val clipData = ClipData.newRawUri(flag.label, uri)
        clipboardManager.setPrimaryClip(clipData)
    }

    override fun observeClipboardChange(action: (data: String) -> Boolean) {
        if (this.onPerformAction !== action) removeClipboardObserver()
        this.onPerformAction = action
        if (!isObserving)
            clipboardManager.addPrimaryClipChangedListener(clipboardListener)
        isObserving = true
    }

    override fun removeClipboardObserver() {
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        this.onPerformAction = null
        isObserving = false
    }

    override fun isObserving(): Boolean = isObserving

    private val clipboardListener = object : ClipboardManager.OnPrimaryClipChangedListener {
        override fun onPrimaryClipChanged() {
            if (!isRecording) return
            val clipData = clipboardManager.primaryClip ?: return
            val label = clipData.description.label

            if (label == ClipboardProviderFlags.IgnorePrimaryChangeListener.label) return

            val data = clipData.getItemAt(0)?.coerceToText(context)?.toString()

            if (data != null) {
                val predicate = if (label != ClipboardProviderFlags.IgnoreObservedAction.label) {
                    onPerformAction?.invoke(data) == true
                } else true
                if (predicate) {
                    setCurrentClip(data)
                }
            }
            Log.e(TAG, "Data: ${clipboardManager.primaryClip?.getItemAt(0)?.text}")
        }
    }
}