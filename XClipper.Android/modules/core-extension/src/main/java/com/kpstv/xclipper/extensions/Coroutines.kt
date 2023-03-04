package com.kpstv.xclipper.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun launchInIO(block: suspend (CoroutineScope.() -> Unit)) {
    CoroutineScope(Dispatchers.IO).launch(block = block)
}

fun launchInMain(block: suspend (CoroutineScope.() -> Unit)) {
    CoroutineScope(Dispatchers.Main).launch(block = block)
}

fun<T> Flow<T>.collectIn(lifecycleOwner: LifecycleOwner, block: FlowCollector<T>) {
    lifecycleOwner.lifecycleScope.launch {
        collect(block)
    }
}