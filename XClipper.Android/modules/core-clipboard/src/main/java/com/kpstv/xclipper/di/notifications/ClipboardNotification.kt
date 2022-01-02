package com.kpstv.xclipper.di.notifications

interface ClipboardNotification {
  fun notifyOnCopy(data: String, withSpecialActions: Boolean = true)
}