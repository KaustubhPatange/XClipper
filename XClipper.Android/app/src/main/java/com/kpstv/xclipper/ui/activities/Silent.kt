package com.kpstv.xclipper.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.xclipper.App
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.ui.fragments.MoreBottomSheet
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// TODO: Seems like an unused Activity, was initial FragmentActivity

@AndroidEntryPoint
class Silent : AppCompatActivity() {

    @Inject lateinit var repository: MainRepository
    @Inject lateinit var tinyUrlApiHelper: TinyUrlApiHelper
    @Inject lateinit var dictionaryApiHelper: DictionaryApiHelper
    @Inject lateinit var clipboardProvider: ClipboardProvider

    private var isShown = false

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus)
            handleIncomingIntent(intent)
    }

    /** A method to show bottom sheet for special actions for notification. */
    private fun handleIncomingIntent(intent: Intent?) {

        if (isShown) return

        if (intent?.action == App.ACTION_SMART_OPTIONS) {

            val data = intent.getStringExtra(App.APP_CLIP_DATA)

            ioThread {
                if (data != null) {
                    val clip = repository.getData(data)
                    clip?.let {
                        mainThread {
                            isShown = true
                            MoreBottomSheet(
                                tinyUrlApiHelper = tinyUrlApiHelper,
                                dictionaryApiHelper = dictionaryApiHelper,
                                clipboardProvider = clipboardProvider,
                                clip = it,
                                supportFragmentManager = supportFragmentManager
                            ).show(supportFragmentManager, "blank")
                        }
                    }
                }
            }
        }
    }
}