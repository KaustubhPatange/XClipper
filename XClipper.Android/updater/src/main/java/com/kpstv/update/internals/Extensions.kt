package com.kpstv.update

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resume

suspend fun Call.await(): Result<Response> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resume(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful)
                    continuation.resume(Result.success(response))
                else
                    continuation.resume(Result.failure(IllegalStateException("The response was not successful")))
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

suspend fun OkHttpClient.get(url: String): Result<Response> {
    return newCall(Request.Builder().url(url).build()).await()
}