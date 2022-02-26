package com.kpstv.xclipper.ui.navigation

// Consuming navigation event is important as it could duplicate
// navigation to the same destination if configuration change happens.
abstract class AbstractNavigationOptions {
    private var consumed: Boolean = false

    fun isConsumed() = consumed
    fun consumed() {
        consumed = true
    }
}

object AbstractNavigationOptionsExtensions {
    @Suppress("UNCHECKED_CAST")
    fun AbstractNavigationOptions.consume(block: () -> Unit) {
        if (!isConsumed()) {
            consumed()
            block()
        }
    }
}