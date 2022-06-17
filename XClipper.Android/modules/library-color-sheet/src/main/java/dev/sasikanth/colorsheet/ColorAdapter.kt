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

package dev.sasikanth.colorsheet

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.sasikanth.colorsheet.utils.isColorDark
import dev.sasikanth.colorsheet.utils.resolveColor
import dev.sasikanth.colorsheet.utils.resolveColorAttr

internal class ColorAdapter(
    private val dialog: ColorSheet?,
    private var colors: IntArray,
    private val selectedColor: Int?,
    private val noColorOption: Boolean,
    private val listener: ColorPickerListener
) : RecyclerView.Adapter<ColorAdapter.ColorItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val color = inflater.inflate(R.layout.color_item, parent, false)
        return ColorItemViewHolder(color)
    }

    override fun getItemCount(): Int {
        return if (noColorOption) {
            colors.size + 1
        } else {
            colors.size
        }
    }

    override fun onBindViewHolder(holder: ColorItemViewHolder, position: Int) {
        holder.bindView()
    }

    inner class ColorItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val colorSelected = itemView.findViewById<ImageView>(R.id.colorSelected)
        private val colorSelectedCircle = itemView.findViewById<ImageView>(R.id.colorSelectedCircle)

        private val circle by lazy {
            ContextCompat.getDrawable(itemView.context, R.drawable.ic_circle)
        }
        private val check by lazy {
            ContextCompat.getDrawable(itemView.context, R.drawable.ic_check)
        }
        private val noColor by lazy {
            ContextCompat.getDrawable(itemView.context, R.drawable.ic_no_color)
        }

        init {
            itemView.setOnClickListener(this)
        }

        fun bindView() {
            if (noColorOption) {
                if (adapterPosition != 0) {
                    val color = colors[adapterPosition - 1]
                    bindColorView(color)
                } else {
                    bindNoColorView()
                }
            } else {
                val color = colors[adapterPosition]
                bindColorView(color)
            }
        }

        private fun bindColorView(@ColorInt color: Int) {
            colorSelected.isVisible = selectedColor != null && selectedColor == color
            colorSelected.setImageResource(R.drawable.ic_check)
            if (color.isColorDark()) {
                colorSelected.imageTintList =
                    ColorStateList.valueOf(resolveColor(itemView.context, android.R.color.white))
            } else {
                colorSelected.imageTintList =
                    ColorStateList.valueOf(resolveColor(itemView.context, android.R.color.black))
            }
            colorSelectedCircle.imageTintList = ColorStateList.valueOf(color)
        }

        private fun bindNoColorView() {
            if (selectedColor != null && selectedColor == ColorSheet.NO_COLOR) {
                colorSelected.isVisible = true
                colorSelected.setImageDrawable(check)
            } else {
                colorSelected.isVisible = true
                colorSelected.setImageDrawable(noColor)
            }
            colorSelectedCircle.background = circle
            colorSelectedCircle.imageTintList = ColorStateList.valueOf(
                resolveColorAttr(itemView.context, attrRes = R.attr.dialogPrimaryVariant)
            )
        }

        override fun onClick(v: View?) {
            if (noColorOption) {
                if (adapterPosition == 0) {
                    listener?.invoke(ColorSheet.NO_COLOR)
                } else {
                    listener?.invoke(colors[adapterPosition - 1])
                }
            } else {
                listener?.invoke(colors[adapterPosition])
            }
            dialog?.dismiss()
        }
    }
}
