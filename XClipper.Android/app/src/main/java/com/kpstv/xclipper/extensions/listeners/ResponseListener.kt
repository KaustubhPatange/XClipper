package com.kpstv.xclipper.extensions.listeners

/**
 * A response listener to use whenever there is a processing of data
 * in a different thread and want to send result through a callback.
 *
 * Generally made to make retrofit callbacks easier with deferred executions.
 */
class ResponseListener<T>(
    private val complete: (data: T) -> Unit,
    private val error: (e: Exception) -> Unit
): ResponseActions<T> {
    override fun onComplete(data: T) {
        complete.invoke(data)
    }

    override fun onError(e: Exception) {
        error.invoke(e)
    }
}

interface ResponseActions<T> {
    fun onComplete(data: T)
    fun onError(e: Exception)
}