package com.kpstv.xclipper.extensions.listeners

/**
 * A response listener to use whenever there is a processing of data
 * in a different thread and want to send result through a callback.
 *
 * Generally made to make retrofit callbacks easier with deferred executions.
 */

sealed class ResponseResult<T> {
    data class Complete<T>(val data: T): ResponseResult<T>()
    data class Error<T>(val error: Exception): ResponseResult<T>()

    companion object {
        fun<T> complete(data: T): ResponseResult<T> = Complete(data)
        fun<T> error(e: Exception): ResponseResult<T> = Error(e)
        fun<T> error(message: String): ResponseResult<T> = Error(Exception(message))
    }
}

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