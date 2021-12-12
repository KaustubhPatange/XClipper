package com.kpstv.xclipper.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.launchInIO
import com.kpstv.xclipper.extensions.launchInMain
import com.kpstv.xclipper.ui.fragments.sheets.SpecialBottomSheet
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * When "Special Actions" is clicked from notifications this activity will be launched.
 */
@AndroidEntryPoint
class SpecialActions : AppCompatActivity() {

    @Inject
    lateinit var repository: MainRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getStringExtra(APP_CLIP_DATA)

        if (data == null) {
            finish()
            return
        }

        AppThemeHelper.applyDialogTheme(this)

        launchInIO {
            val clip = repository.getData(data)

            if (clip == null) {
                launchInMain { finish() }
                return@launchInIO
            }

            launchInMain {
                SpecialBottomSheet.show(
                    activity = this@SpecialActions,
                    onClose = ::finish,
                    clip = clip
                )
            }
        }
    }

    companion object {
        private const val APP_CLIP_DATA = "com.kpstv.xclipper.clip_data"
        fun launch(context: Context, clipData: String) {
            val newIntent = launchIntent(context, clipData)
            context.startActivity(newIntent)
        }

        fun launchIntent(context: Context, clipData: String): Intent =
            Intent(context, SpecialActions::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse(clipData)
                putExtra(APP_CLIP_DATA, clipData)
            }
    }
}