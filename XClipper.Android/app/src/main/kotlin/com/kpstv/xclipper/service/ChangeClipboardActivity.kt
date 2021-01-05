package com.kpstv.xclipper.service

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.ui.helpers.ClipRepositoryHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class ChangeClipboardActivity : FragmentActivity() {

    @Inject lateinit var clipProvider: ClipboardProvider
    @Inject lateinit var clipRepositoryHelper: ClipRepositoryHelper

    private val TAG = javaClass.simpleName

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.e(TAG, "Focus Changed: $hasFocus")

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        lifecycleScope.launchWhenCreated {
            /** Seems like adding a delay is giving [ClipboardManager] time to capture
             *  clipboard text.
             */
            delay(500)
            if (hasFocus) {
                val data = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
                saveData(data)
                finish()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        onWindowFocusChanged(true)
    }

    private fun saveData(data: String?) {
        if (data != null && CLIP_DATA != data) {
            CLIP_DATA = data

            /** Set current clip */
            clipProvider.setCurrentClip(data)

            // Save data and exit
            clipRepositoryHelper.insertOrUpdateClip(data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "Destroyed()")
    }
}