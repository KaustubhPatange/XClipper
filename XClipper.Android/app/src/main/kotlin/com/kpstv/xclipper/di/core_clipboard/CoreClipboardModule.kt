package com.kpstv.xclipper.di.core_clipboard

import com.kpstv.xclipper.di.core_clipboard.notification.ClipboardNotificationImpl
import com.kpstv.xclipper.di.notifications.ClipboardNotification
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreClipboardModule {
  @Binds
  abstract fun clipboardNotifications(clipboardNotificationImpl: ClipboardNotificationImpl): ClipboardNotification
}