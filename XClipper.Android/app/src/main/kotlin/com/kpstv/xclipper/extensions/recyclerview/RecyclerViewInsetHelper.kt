package com.kpstv.xclipper.extensions.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A helper class to automatically pad the last/first item based on system insets.
 */
class RecyclerViewInsetHelper : RecyclerView.ItemDecoration() {
    private var insetType: InsetType = InsetType.NONE
    private var gap: Int = 0

    fun attach(recyclerView: RecyclerView, type: InsetType, extra: Boolean = false) {
        insetType = type
        recyclerView.setOnApplyWindowInsetsListener { v, insets ->
            gap = when(type) {
                InsetType.TOP -> insets.systemWindowInsetTop
                InsetType.BOTTOM -> insets.systemWindowInsetBottom
                InsetType.RIGHT -> insets.systemWindowInsetRight
                InsetType.LEFT -> insets.systemWindowInsetLeft
                else -> 0
            }
            if (extra) {
                gap = gap * 2 + (10 * recyclerView.resources.displayMetrics.density).toInt()
            }
            insets
        }
        recyclerView.addItemDecoration(this)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildLayoutPosition(view)
        if (position == (parent.adapter?.itemCount ?: 0) - 1) {
            when(insetType) {
                InsetType.TOP -> outRect.top += gap
                InsetType.BOTTOM -> outRect.bottom += gap
                InsetType.RIGHT -> outRect.right += gap
                InsetType.LEFT -> outRect.left += gap
                else -> {}
            }
        }
    }

    enum class InsetType {
        NONE,
        TOP,
        BOTTOM,
        RIGHT,
        LEFT
    }
}