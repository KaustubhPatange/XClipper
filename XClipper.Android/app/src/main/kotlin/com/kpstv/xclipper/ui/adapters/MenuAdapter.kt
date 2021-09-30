package com.kpstv.xclipper.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.SpecialMenu
import com.kpstv.xclipper.extensions.utils.Utils.Companion.getDataFromAttr
import kotlinx.android.synthetic.main.item_special.view.*

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

    private fun MenuHolder.bind(item: SpecialMenu) = with(itemView) {
        is_text.text = item.title
        is_image.setImageDrawable(ContextCompat.getDrawable(context, item.image))

        val defaultThemeColor = getDataFromAttr(
            context = context,
            attr = R.attr.colorTextSecondaryLight
        )

        if (item.imageTint == -1)
            is_image.imageTintList = ColorStateList.valueOf(defaultThemeColor)
        else
            is_image.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, item.imageTint))

        if (item.textColor == -1)
            is_text.setTextColor(defaultThemeColor)
        else
            is_text.setTextColor(ContextCompat.getColor(context, item.textColor))

        mainLayout.setOnClickListener { item.onClick.invoke() }
    }


    override fun getItemCount() = list.size

    class MenuHolder(view: View) : RecyclerView.ViewHolder(view)
}