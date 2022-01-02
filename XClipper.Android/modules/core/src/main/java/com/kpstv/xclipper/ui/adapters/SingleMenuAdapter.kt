package com.kpstv.xclipper.ui.adapters

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.core.R
import com.kpstv.xclipper.data.model.SingleMenuItem
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.layoutInflater

/**
 * A reusable Adapter!
 *
 * Supports a layout whose,
 * 1. root layout is clickable & focusable
 * 2. Has TextView with id (R.id.menu_text)
 * 3. Has ImageView with id (R.id.menu_image)
 */
class SingleMenuAdapter(
    val list: ArrayList<SingleMenuItem>,
    @LayoutRes
    val itemLayout: Int

) : RecyclerView.Adapter<SingleMenuAdapter.MenuHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MenuHolder(parent.context.layoutInflater().inflate(itemLayout, parent, false))

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    class MenuHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(R.id.menu_text)
        private val imageView = view.findViewById<ImageView>(R.id.menu_image)

        fun bind(item: SingleMenuItem) {
            textView.text = item.title
            imageView.setImageDrawable(ContextCompat.getDrawable(view.context, item.image))

            val defaultThemeColor = view.context.getColorAttr(R.attr.colorTextSecondaryLight)

            if (item.imageTint == -1)
                imageView.imageTintList = ColorStateList.valueOf(defaultThemeColor)
            else
                imageView.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(view.context, item.imageTint))

            if (item.textColor == -1)
                textView.setTextColor(defaultThemeColor)
            else
                textView.setTextColor(ContextCompat.getColor(view.context, item.textColor))

            view.setOnClickListener { item.onClick.invoke() }
        }
    }
}