package com.kpstv.xclipper.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.di.action.SpecialActionOption
import com.kpstv.xclipper.extensions.launchInIO
import com.kpstv.xclipper.ui.fragments.sheets.SpecialBottomSheet
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
        val option = intent.getParcelableExtra(SPECIAL_OPTIONS) ?: SpecialActionOption()

        if (data == null) {
            finish()
            return
        }

        AppThemeHelper.applyDialogTheme(this)

        launchInIO {
            val clip = repository.getClipByData(data)

            if (clip == null) {
                lifecycleScope.launch { finish() }
                return@launchInIO
            }

            lifecycleScope.launch {
                SpecialBottomSheet.show(
                    activity = this@SpecialActions,
                    clip = clip,
                    option = option,
                    onClose = ::finish
                )
            }
        }
    }

    companion object {
        private const val APP_CLIP_DATA = "com.kpstv.xclipper.clip_data"
        private const val SPECIAL_OPTIONS = "com.kpstv.xclipper.special_options"
        fun launch(context: Context, clipData: String, option: SpecialActionOption = SpecialActionOption()) {
            val newIntent = launchIntent(context, clipData, option)
            context.startActivity(newIntent)
        }

        fun launchIntent(context: Context, clipData: String, option: SpecialActionOption = SpecialActionOption()): Intent =
            Intent(context, SpecialActions::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse(clipData)
                putExtra(APP_CLIP_DATA, clipData)
                putExtra(SPECIAL_OPTIONS, option)
            }
    }
}