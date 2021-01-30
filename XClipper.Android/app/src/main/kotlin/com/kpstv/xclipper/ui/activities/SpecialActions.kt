package com.kpstv.xclipper.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.ui.fragments.sheets.MoreBottomSheet
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * When "Special Actions" is clicked from notifications this activity will be launched.
 */
@AndroidEntryPoint
class SpecialActions : AppCompatActivity() {

    @Inject lateinit var repository: MainRepository
    @Inject lateinit var tinyUrlApiHelper: TinyUrlApiHelper
    @Inject lateinit var dictionaryApiHelper: DictionaryApiHelper
    @Inject lateinit var clipboardProvider: ClipboardProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getStringExtra(APP_CLIP_DATA)

        if (data == null) {
            finish()
            return
        }

        ThemeUtils.setDialogTheme(this)

        ioThread {
            val clip = repository.getData(data)

            if (clip == null) {
                mainThread { finish() }
                return@ioThread
            }

            mainThread {
                MoreBottomSheet(
                    tinyUrlApiHelper = tinyUrlApiHelper,
                    dictionaryApiHelper = dictionaryApiHelper,
                    clipboardProvider = clipboardProvider,
                    supportFragmentManager = supportFragmentManager,
                    onClose = ::finish,
                    clip = clip
                ).show(
                    supportFragmentManager,
                    "blank"
                )
            }
        }
    }
}