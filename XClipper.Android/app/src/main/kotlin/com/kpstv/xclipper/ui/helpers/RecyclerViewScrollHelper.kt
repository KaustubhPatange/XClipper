package com.kpstv.xclipper.ui.helpers

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.extensions.SimpleFunction

class RecyclerViewScrollHelper : RecyclerView.OnScrollListener() {

    var verticalOffset = 0
        private set
    private var isBtnShown = false

    private var onScrollUp: SimpleFunction? = null
    private var onScrollDown: SimpleFunction? = null
    private var recyclerView: RecyclerView? = null

    fun attach(recyclerView: RecyclerView, onScrollDown: SimpleFunction, onScrollUp: SimpleFunction) {
        recyclerView.addOnScrollListener(this)
        this.recyclerView = recyclerView
        this.onScrollDown = onScrollDown
        this.onScrollUp = onScrollUp
    }

    fun reset() {
        recyclerView?.smoothScrollToPosition(0)
        onScrollStateChanged(recyclerView!!, 0)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        verticalOffset = recyclerView.computeVerticalScrollOffset()
        if (verticalOffset > 10 && !isBtnShown) {
            onScrollDown?.invoke()
            isBtnShown = true
        } else if (verticalOffset < 10 && isBtnShown) {
            onScrollUp?.invoke()
            isBtnShown = false
        }
    }
}