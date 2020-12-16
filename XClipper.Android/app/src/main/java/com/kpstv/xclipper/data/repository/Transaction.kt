package com.kpstv.xclipper.data.repository

import kotlinx.coroutines.*

/**
 * A transaction to carry out insert in thread-safe way also not to
 * overwhelm inserts in database.
 *
 * It will carry out the operation after threshold limit is reached
 * till that time it will keep on polling the [Job].
 */
class Transaction<T>(
    private val thresholdMilliseconds: Long = 500,
    private val transaction: (List<T>) -> Unit
) {
    private val list = ArrayList<T>()
    private var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler {_,_ ->
        // do nothing
    }

    fun add(item: T) {
        job?.cancel()
        list.add(item)
        job = CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            delay(thresholdMilliseconds)
            // Ready for transaction
            transaction.invoke(list)
            list.clear()
            cancel()
        }
    }
}