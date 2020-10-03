package com.kpstv.xclipper.extensions.listeners

import com.kpstv.xclipper.extensions.ErrorFunction
import com.kpstv.xclipper.extensions.SimpleFunction

/**
 * This class provides two listener mainly for performing actions after
 * success/complete or error.
 *
 * @param onBefore Used to execute some codes before the callbacks are invoked.
 * @param onComplete Receives a callback when task is succeeded or completed.
 * @param onError Receives a callback when an error occurred.
 * @param onAfter Used to execute some codes after the callbacks are invoked.
 */
class StatusListener(
    private val onBefore: SimpleFunction? = null,
    private val onComplete: SimpleFunction,
    private val onError: SimpleFunction,
    private val onAfter: SimpleFunction? = null
): CompleteAction {
    override fun onComplete() {
        onBefore?.invoke()
        onComplete.invoke()
        onAfter?.invoke()
    }

    override fun onError() {
        onBefore?.invoke()
        onError.invoke()
        onAfter?.invoke()
    }
}

private interface CompleteAction {
    fun onComplete()
    fun onError()
}