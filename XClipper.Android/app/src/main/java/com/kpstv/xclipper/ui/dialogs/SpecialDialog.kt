package com.kpstv.xclipper.ui.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.SpecialHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.special_layout.view.*
import javax.inject.Inject

@AndroidEntryPoint
class SpecialDialog : AppCompatActivity() {

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

        setContentView(R.layout.special_layout)

        with(window.decorView.rootView) {
            ioThread {
                val clip = repository.getData(data)

                if (clip == null) {
                    mainThread { finish() }
                    return@ioThread
                }

                mainThread {
                    toolbar.navigationIcon =
                        ContextCompat.getDrawable(this@SpecialDialog, R.drawable.ic_close)
                    toolbar.setNavigationOnClickListener { finish() }

                    btn_ok.setOnClickListener { finish() }

                    SpecialHelper(
                        context = this@SpecialDialog,
                        dictionaryApiHelper = dictionaryApiHelper,
                        tinyUrlApiHelper = tinyUrlApiHelper,
                        clipboardProvider = clipboardProvider,
                        supportFragmentManager = supportFragmentManager,
                        clip = clip,
                        isDialog = true
                    ).setActions(this) {
                        finish()
                    }
                }
            }
        }
    }
}