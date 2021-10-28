package com.kpstv.xclipper.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.extensions.utils.Utils.Companion.getDataFromAttr

class MenuAdapter(
    val list: ArrayList<SpecialMenu>,
    @LayoutRes
    val itemLayout: Int
) : RecyclerView.Adapter<MenuAdapter.MenuHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MenuHolder(
            LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        )

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    class MenuHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(R.id.is_text)
        private val imageView = view.findViewById<ImageView>(R.id.is_image)

        fun bind(item: SpecialMenu) {
            textView.text = item.title
            imageView.setImageDrawable(ContextCompat.getDrawable(view.context, item.image))

            val defaultThemeColor = getDataFromAttr(
                context = view.context,
                attr = R.attr.colorTextSecondaryLight
            )

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