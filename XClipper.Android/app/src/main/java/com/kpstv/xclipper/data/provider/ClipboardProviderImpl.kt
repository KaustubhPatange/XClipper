package com.kpstv.xclipper.data.provider

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.App
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isPackageBlacklisted
import com.kpstv.xclipper.service.ClipboardAccessibilityService.Companion.currentPackage

class ClipboardProviderImpl(
    private val context: Context,
    private val repository: MainRepository
) : ClipboardProvider {

    private val _currentClip = MutableLiveData<String>()
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
        with(context) {
            clipboardManager.addPrimaryClipChangedListener {
                if (!isRecording) return@addPrimaryClipChangedListener
                if (isPackageBlacklisted(currentPackage)) return@addPrimaryClipChangedListener

                val data = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
                if (data != null && App.CLIP_DATA != data) {
                    App.CLIP_DATA = data
                    setCurrentClip(data)

                    repository.updateRepository(App.CLIP_DATA)
                }
                Log.e(TAG, "Data: ${clipboardManager.primaryClip?.getItemAt(0)?.text}")
            }
        }
    }
}