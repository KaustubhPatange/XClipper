package com.kpstv.xclipper.data.provider

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import com.kpstv.xclipper.App
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isPackageBlacklisted
import com.kpstv.xclipper.service.ClipboardAccessibilityService.Companion.currentPackage

class ClipboardProviderImpl(
    private val context: Context,
    private val repository: MainRepository
) : ClipboardProvider {
    private val TAG = javaClass.simpleName
    override fun observeClipboardChange() {
        with(context) {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.addPrimaryClipChangedListener {
                if (isPackageBlacklisted(currentPackage)) return@addPrimaryClipChangedListener

                val data = clipboardManager.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
                if (data != null && App.CLIP_DATA != data) {
                    App.CLIP_DATA = data
                    repository.setCurrentClip(data)

                    repository.updateRepository(App.CLIP_DATA)
                }
                Log.e(TAG, "Data: ${clipboardManager.primaryClip?.getItemAt(0)?.text}")
            }
        }
    }
}