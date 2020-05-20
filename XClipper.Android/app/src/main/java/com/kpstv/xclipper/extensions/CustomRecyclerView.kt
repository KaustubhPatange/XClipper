package com.kpstv.xclipper.extensions

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerView : RecyclerView {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context!!, attrs, defStyle) {
    }

    override fun scrollTo(x: Int, y: Int) {
        /** Overriding on scrollTo function to actually remove the error
         * unsupported scrolling to absolute position */
    }

    companion object {
        private const val TAG = "CustomRecyclerView"
    }
}