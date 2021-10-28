package com.kpstv.xclipper.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun launchInIO(block: suspend (CoroutineScope.() -> Unit)) {
    CoroutineScope(Dispatchers.IO).launch(block = block)
}

fun launchInMain(block: suspend (CoroutineScope.() -> Unit)) {
    CoroutineScope(Dispatchers.Main).launch(block = block)
}