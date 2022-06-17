package dev.sasikanth.colorsheet.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import dev.sasikanth.colorsheet.R

internal class ColorSheetTitle
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : BaselineGridTextView(context, attrs, defStyleAttr) {

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ColorSheetTitle, defStyleAttr, 0
        )
        setLineHeightHint(
            a.getDimensionPixelSize(
                R.styleable.ColorSheetTitle_colorSheetTitleLineHeightHint,
                0
            ).toFloat()
        )

        val fontResId = a.getResourceId(R.styleable.ColorSheetTitle_colorSheetTitleFont, 0)
        if (fontResId != 0) {
            val font = ResourcesCompat.getFont(context, fontResId)
            typeface = font
        }

        letterSpacing = a.getFloat(R.styleable.ColorSheetTitle_colorSheetTitleLetterSpacing, 0f)
        a.recycle()
    }
}
