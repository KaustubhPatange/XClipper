package com.kpstv.xclipper.di.notifications

interface ClipboardNotification {
  fun notifyOnCopy(data: String, withCopyButton: Boolean = false, withSpecialActions: Boolean = true)
}