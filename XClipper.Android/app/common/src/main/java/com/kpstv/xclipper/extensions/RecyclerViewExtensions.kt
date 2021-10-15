package com.kpstv.xclipper.extensions

import androidx.annotation.ColorInt
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * Highlight specific item in recyclerview. Setting color to -1 will auto determine
 * the lighter & darker versions of the color.
 */
fun RecyclerView.highlightChildPosition(index: Int, @ColorInt color: Int = -1) {
    val layoutManager = layoutManager ?: throw IllegalStateException("No layout manager is set")

    doOnLayout {
        val itemView = layoutManager.findViewByPosition(index)
        if (itemView == null) {
            smoothScrollToPosition(index)
            doOnLayout {
                layoutManager.findViewByPosition(index)?.runBlinkEffect(color)
            }
        } else {
            itemView.runBlinkEffect(color)
        }
    }
}