package com.kpstv.xclipper.ui.activities

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.kpstv.xclipper.App
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.ui.fragments.MoreBottomSheet
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class Silent : FragmentActivity(), KodeinAware {
    override val kodein by kodein()
    private val repository by instance<MainRepository>()
    private val tinyUrlApiHelper by instance<TinyUrlApiHelper>()
    private val dictionaryApiHelper by instance<DictionaryApiHelper>()

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