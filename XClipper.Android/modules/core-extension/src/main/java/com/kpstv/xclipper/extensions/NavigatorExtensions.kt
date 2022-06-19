package com.kpstv.xclipper.extensions

import androidx.fragment.app.Fragment
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.clearArgs
import com.kpstv.navigation.getKeyArgs

inline fun <reified T: BaseArgs> Fragment.consumeArgs(): T {
    val args = getKeyArgs<T>()
    clearArgs<T>()
    return args
}