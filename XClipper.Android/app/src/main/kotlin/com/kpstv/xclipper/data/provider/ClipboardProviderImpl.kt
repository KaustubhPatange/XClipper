package com.kpstv.xclipper.data.provider

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isPackageBlacklisted
import com.kpstv.xclipper.service.ClipboardAccessibilityService.Companion.currentPackage
import com.kpstv.xclipper.ui.helpers.ClipRepositoryHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ClipboardProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipRepositoryHelper: ClipRepositoryHelper
) : ClipboardProvider {

    private val _currentClip = MutableLiveData<String>()
    private var isObserving = false
    private var isRecording = true

    private val TAG = javaClass.simpleName
    private val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

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

    override fun setClipboard(item: ClipData?) {
        if (item == null) return
        clipboardManager.setPrimaryClip(item)
    }

    override fun observeClipboardChange() {
        if (!isObserving)
            clipboardManager.addPrimaryClipChangedListener(clipboardListener)
        isObserving = true
    }

    override fun removeClipboardObserver() {
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        isObserving = false
    }

    override fun isObserving(): Boolean = isObserving

    private val clipboardListener = object : ClipboardManager.OnPrimaryClipChangedListener {
        override fun onPrimaryClipChanged() {
            if (!isRecording) return
            if (isPackageBlacklisted(currentPackage)) return

            val data = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
            if (data != null) {
                setCurrentClip(data)

                clipRepositoryHelper.insertOrUpdateClip(data)
            }
            Log.e(TAG, "Data: ${clipboardManager.primaryClip?.getItemAt(0)?.text}")
        }
    }
}