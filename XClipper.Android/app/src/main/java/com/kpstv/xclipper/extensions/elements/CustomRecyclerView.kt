package com.kpstv.xclipper.extensions.elements

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.R

/**
 * A class created generally to avoid the "unsupported scrolling to absolute position"
 * error when "animateLayoutChanges" is set to true in parent layout.
 *
 * Now also provides an extension to standard recyclerView which allows you to set
 * "maxHeight" parameter. Once reached it will start showing scrollbars vertically.
 */
class CustomRecyclerView : RecyclerView {
    private var maxHeight = 0
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init(context, attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context!!, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?
    ) {
        val arr = context.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView)
        maxHeight = arr.getLayoutDimension(R.styleable.CustomRecyclerView_maxHeight, maxHeight)
        arr.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var hms = heightMeasureSpec
        if (maxHeight > 0) {
            hms = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthMeasureSpec, hms)
    }

    override fun scrollTo(x: Int, y: Int) {
        /** Overriding on scrollTo function to actually remove the error
         * unsupported scrolling to absolute position */
    }

    companion object {
        private const val TAG = "CustomRecyclerView"
    }
}