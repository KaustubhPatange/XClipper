package com.kpstv.xclipper.extensions.elements

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Workaround for Hilt dependency injection for broadcast receiver as mentioned
 *
 * @see <a href="https://github.com/google/dagger/issues/1918">related issue</a>
 */
abstract class AbstractBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {}
}