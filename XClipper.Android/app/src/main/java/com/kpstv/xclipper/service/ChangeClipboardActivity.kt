package com.kpstv.xclipper.service

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.repository.MainRepository
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class ChangeClipboardActivity : Activity(), KodeinAware {

    override val kodein by closestKodein()
    private val repository: MainRepository by instance()

    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      /*  setContentView(R.layout.empty_view)
        window.currentFocus?.requestFocus()*/
       // window.setLocalFocus(true,false)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.e(TAG, "Focus Changed: $hasFocus")

        val clipboardManager =   getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var data = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()

        if (hasFocus) {
            data = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
            saveData(data)
            finish()
        } else {
            saveData(data)
        }
    }

    private fun saveData(data: String?) {
        if (data != null && CLIP_DATA != data) {
            CLIP_DATA = data
            // Save data and exit
            repository.updateRepository(CLIP_DATA)
        }
    }


}