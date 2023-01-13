package com.kpstv.xclipper.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun launchInIO(block: suspend (CoroutineScope.() -> Unit)) {
    CoroutineScope(Dispatchers.IO).launch(block = block)
}

fun launchInMain(block: suspend (CoroutineScope.() -> Unit)) {
    CoroutineScope(Dispatchers.Main).launch(block = block)
}

inline fun<T> Flow<T>.collectIn(lifecycleOwner: LifecycleOwner, crossinline block: suspend (T) -> Unit) {
    lifecycleOwner.lifecycleScope.launch {
        collect(block)
    }
}