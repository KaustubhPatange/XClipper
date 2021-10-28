package com.kpstv.xclipper.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.xclipper.App.APP_CLIP_DATA
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.launchInIO
import com.kpstv.xclipper.extensions.launchInMain
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
    @Inject lateinit var preferenceProvider: PreferenceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getStringExtra(APP_CLIP_DATA)

        if (data == null) {
            finish()
            return
        }

        ThemeUtils.setDialogTheme(this)

        launchInIO {
            val clip = repository.getData(data)

            if (clip == null) {
                launchInMain { finish() }
                return@launchInIO
            }

            launchInMain {
                MoreBottomSheet.show(
                    activity = this@SpecialActions,
                    onClose = ::finish,
                    clip = clip,
                    preferenceProvider = preferenceProvider
                )
            }
        }
    }

    companion object {
        fun launch(context: Context, clipData: String) {
            val newIntent = Intent(context, SpecialActions::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse(clipData)
                putExtra(APP_CLIP_DATA, clipData)
            }
            context.startActivity(newIntent)
        }
    }
}