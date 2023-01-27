package com.kpstv.xclipper.ui.helpers

import android.content.Context
import android.content.res.ColorStateList
import com.google.android.material.chip.Chip
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.enumerations.SpecialTagFilter
import com.kpstv.xclipper.extensions.drawableFrom
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.toPx
import com.kpstv.xclipper.feature_home.R
import com.kpstv.xclipper.ui.fragments.Home

object TagsUiHelper {

    /**
     * Create tag chip based on the Tag (eg: date, email, url, etc.) for [Home].
     */
    fun createFilterTagChip(context: Context, tag: Tag) : Chip = with(context) {
        return@with Chip(this).apply {
            text = tag.name
            setTag(tag) // to identify the tag associated with the chip
            chipIcon = drawableFrom(R.drawable.ic_tag)
            chipIconTint = ColorStateList.valueOf(getColorAttr(R.attr.colorTextPrimary))
            chipIconSize = toPx(20)
            chipStartPadding = toPx(10)
        }
    }

    /**
     * Create special tag chip based on the SpecialTagFilter (eg: Invert, etc.)
     */
    fun createSpecialTagFilterChip(context: Context, tag: SpecialTagFilter) : Chip = with(context) {
        return@with Chip(this).apply {
            text = getString(tag.stringRes)
            setTag(tag) // to identify the tag associated with
            setTextColor(getColorAttr(R.attr.colorForeground))
            chipBackgroundColor = ColorStateList.valueOf(getColorAttr(R.attr.colorSpecialTag))
            chipIcon = drawableFrom(tag.drawableRes)
            chipIconTint = ColorStateList.valueOf(getColorAttr(R.attr.colorForeground))
            chipIconSize = toPx(20)
            closeIconTint = ColorStateList.valueOf(getColorAttr(R.attr.colorForeground))
        }
    }
}