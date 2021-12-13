package com.kpstv.xclipper.di.core_clipboard.suggestions

import android.content.Context
import android.content.Intent
import com.kpstv.xcipper.service.BubbleService
import com.kpstv.xclipper.di.suggestions.SuggestionService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SuggestionServiceImpl @Inject constructor(
  @ApplicationContext private val context: Context
) : SuggestionService {
  override fun start() : Unit = with(context) {
    startService(Intent(this, BubbleService::class.java))
  }

  override fun stop() : Unit = with(context) {
    stopService(Intent(this, BubbleService::class.java))
  }

  override fun broadcastNodeInfo(nodeText: String, cursorPosition: Int) = with(context) {
    BubbleService.Actions.sendNodeInfo(this, nodeText.toString(), cursorPosition)
  }

  override fun broadcastCloseState() = with(context) {
    BubbleService.Actions.sendCloseState(this)
  }
}