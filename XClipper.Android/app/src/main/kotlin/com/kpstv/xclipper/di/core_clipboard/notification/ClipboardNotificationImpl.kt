package com.kpstv.xclipper.di.core_clipboard.notification

import android.content.Context
import com.kpstv.xclipper.di.notifications.ClipboardNotification
import com.kpstv.xclipper.ui.helpers.Notifications
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// A drop in component to send clipboard notifications like on copy clipboard
// cc: ClipboardNotification interface
class ClipboardNotificationImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ClipboardNotification {
    override fun notifyOnCopy(
        data: String,
        withCopyButton: Boolean,
        withSpecialActions: Boolean
    ) {
        Notifications.sendClipboardCopiedNotification(
            context = context,
            text = data,
            withCopyButton = withCopyButton,
            withSpecialActions = withSpecialActions
        )
    }
}