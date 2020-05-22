package com.kpstv.xclipper.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Tag

class EditAdapter(
    private val onClick: (Tag, Int) -> Unit
): ListAdapter<Tag, EditAdapter.EditHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag) =
            oldItem == newItem
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EditHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false)
        )

    override fun onBindViewHolder(holder: EditHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun EditHolder.bind(tag: Tag) = with(itemView) {

    }

    class EditHolder(view: View) : RecyclerView.ViewHolder(view)
}