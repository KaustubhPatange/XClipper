package com.kpstv.xclipper.di.suggestions

interface SuggestionService {
  fun start()
  fun stop()

  fun broadcastNodeInfo(nodeText: String, cursorPosition: Int)
  fun broadcastCloseState()
}