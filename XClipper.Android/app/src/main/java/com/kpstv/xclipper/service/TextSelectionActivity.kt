package com.kpstv.xclipper.service

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent.ACTION_PROCESS_TEXT
import android.content.Intent.EXTRA_PROCESS_TEXT
import android.os.Build
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isClipboardAccessibilityServiceRunning
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class TextSelectionActivity : Activity(), KodeinAware {
    override val kodein by kodein()
    private val repository by instance<MainRepository>()
    private val clipboardProvider by instance<ClipboardProvider>()
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
                repository.updateRepository(textData)
            }
        }
        finish()
    }
}