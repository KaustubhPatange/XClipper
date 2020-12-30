package com.kpstv.xclipper.extensions.listeners

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.Exception

typealias CancellationCallback = ((cause: Throwable) -> Unit)

/**
 * Perform a addListenerForSingleValueEvent call on a databaseReference as suspending
 * @param onCancellation action to perform if there is a cancellation
 */
@ExperimentalCoroutinesApi
suspend fun DatabaseReference.awaitSingleValue(onCancellation: CancellationCallback = {}): ResponseResult<DataSnapshot> = suspendCancellableCoroutine { continuation ->
    val valueEventListener = object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
            continuation.resume(ResponseResult.error(e = error.toException()), onCancellation)
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            continuation.resume(ResponseResult.complete(snapshot), onCancellation)
        }
    }
    addListenerForSingleValueEvent(valueEventListener)
    continuation.invokeOnCancellation { removeEventListener(valueEventListener) }
}

/**
 * Performs a setValue event call on databaseReference as suspending.
 */
@ExperimentalCoroutinesApi
suspend fun DatabaseReference.awaitSetValue(value: Any, onCancellation: CancellationCallback = {}): ResponseResult<Unit> = suspendCancellableCoroutine { continuation ->
    val completeListener = DatabaseReference.CompletionListener { error, ref ->
        if (error == null)
            continuation.resume(ResponseResult.complete(Unit), onCancellation)
        else
            continuation.resume(ResponseResult.error(e = error.toException()), onCancellation)
    }
    setValue(value, completeListener)
}

/**
 * A flow for valueEventListener.
 */
@ExperimentalCoroutinesApi
suspend fun DatabaseReference.awaitValueChangeEvent(): Flow<ResponseResult<DataSnapshot>> = callbackFlow {
    val valueEventListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            sendBlocking(ResponseResult.complete(snapshot))
        }
        override fun onCancelled(error: DatabaseError) {
            sendBlocking(ResponseResult.error<Exception>(e = error.toException()))
        }
    }
    addValueEventListener(valueEventListener)
    awaitClose {
        removeEventListener(valueEventListener)
    }
}
