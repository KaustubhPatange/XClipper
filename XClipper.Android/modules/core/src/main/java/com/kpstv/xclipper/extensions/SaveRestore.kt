package com.kpstv.xclipper.extensions

import android.os.Bundle

interface SaveRestore {
    fun saveState(bundle: Bundle)
    fun restoreState(bundle: Bundle?)
}