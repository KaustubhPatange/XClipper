/*
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.colorsheet.utils

import android.content.Context
import androidx.annotation.StyleRes
import dev.sasikanth.colorsheet.R
import android.R.attr as androidAttr

internal enum class Theme(@StyleRes val styleRes: Int) {

    LIGHT(R.style.BaseTheme_ColorSheet_Light),
    DARK(R.style.BaseTheme_ColorSheet_Dark);

    companion object {
        fun inferTheme(context: Context): Theme {
            val isPrimaryDark = resolveColorAttr(
                context = context,
                attrRes = androidAttr.textColorPrimary
            ).isColorDark()
            return if (isPrimaryDark) LIGHT else DARK
        }
    }
}
