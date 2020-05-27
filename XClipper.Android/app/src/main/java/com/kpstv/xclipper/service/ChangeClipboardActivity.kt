package com.kpstv.xclipper.service

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.ACTION_SMART_OPTIONS
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.App.CLIP_DATA
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.ui.fragments.MoreBottomSheet
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class ChangeClipboardActivity : FragmentActivity(), KodeinAware {

    override val kodein by kodein()
    private val repository by instance<MainRepository>()
    private val tinyUrlApiHelper by instance<TinyUrlApiHelper>()
    private val dictionaryApiHelper by instance<DictionaryApiHelper>()

    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.e(TAG, "Focus Changed: $hasFocus")

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (hasFocus) {
            val data = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
            saveData(data)
            //    finishActivity(0)
        }/* else {
            saveData(data)
        }*/
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        onWindowFocusChanged(true)
    }

    private fun saveData(data: String?) {
        if (data != null && CLIP_DATA != data) {
            CLIP_DATA = data
            // Save data and exit
            repository.updateRepository(CLIP_DATA)
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "Destroyed()")
    }
}