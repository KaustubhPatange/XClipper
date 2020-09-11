package com.kpstv.xclipper.ui.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.SpecialHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import kotlinx.android.synthetic.main.special_layout.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SpecialDialog : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val repository by instance<MainRepository>()
    private val tinyUrlApiHelper by instance<TinyUrlApiHelper>()
    private val dictionaryApiHelper by instance<DictionaryApiHelper>()

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