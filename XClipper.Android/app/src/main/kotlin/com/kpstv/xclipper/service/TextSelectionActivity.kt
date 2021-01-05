package com.kpstv.xclipper.service

import android.content.ClipData
import android.content.Intent.ACTION_PROCESS_TEXT
import android.content.Intent.EXTRA_PROCESS_TEXT
import android.os.Build
import androidx.activity.ComponentActivity
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.ui.helpers.ClipRepositoryHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TextSelectionActivity : ComponentActivity() {

    @Inject lateinit var clipboardProvider: ClipboardProvider
    @Inject lateinit var clipRepositoryHelper: ClipRepositoryHelper

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent?.apply {
                if (action != ACTION_PROCESS_TEXT && !hasExtra(EXTRA_PROCESS_TEXT)) return@apply
                val textData = getStringExtra(EXTRA_PROCESS_TEXT)

                /**
                 * This will trigger save twice, one from [MainRepository.updateRepository] &
                 * the other from Accessibility service when we set this data as current clipboard.
                 */
                clipboardProvider.setClipboard(ClipData.newPlainText(null, textData))
                if (textData != null)
                    clipRepositoryHelper.insertOrUpdateClip(textData)
            }
        }
        finish()
    }
}