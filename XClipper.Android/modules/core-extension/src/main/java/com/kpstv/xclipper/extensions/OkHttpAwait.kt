package com.kpstv.xclipper.extensions

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Call.await(): Result<Response> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resume(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(Result.success(response))
            }
        })
        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
            }
        }
    }
}