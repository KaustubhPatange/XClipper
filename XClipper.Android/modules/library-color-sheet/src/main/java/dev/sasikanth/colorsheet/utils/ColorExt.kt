/*
 *   ColorSheet
 *
 *   Copyright (c) 2019. Sasikanth Miriyampalli
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package dev.sasikanth.colorsheet.utils

import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat

/**
 * Get color
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@ColorInt
internal fun resolveColor(context: Context, @ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(context, colorRes)
}

/**
 * Resolve a attribute value and return color
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@ColorInt
internal fun resolveColorAttr(
    context: Context,
    @AttrRes attrRes: Int
): Int {
    val a = context.theme.obtainStyledAttributes(intArrayOf(attrRes))
    return a.getColor(0, 0)
}

/**
 * gotten from [https://github.com/afollestad/material-dialogs/blob/master/core/src/main/java/com/afollestad/materialdialogs/utils/MDUtil.kt]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Int.isColorDark(threshold: Double = 0.5): Boolean {
    if (this == Color.TRANSPARENT) {
        return false
    }
    val darkness =
        1 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
    return darkness >= threshold
}
