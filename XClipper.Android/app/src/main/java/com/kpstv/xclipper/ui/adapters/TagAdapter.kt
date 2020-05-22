package com.kpstv.xclipper.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.DialogState
import com.kpstv.xclipper.data.model.Tag
import kotlinx.android.synthetic.main.tag_item.view.*

class TagAdapter(
    private val dialogState: LiveData<DialogState>,
    private val onClick: (Tag, Int) -> Unit
) : ListAdapter<Tag, TagAdapter.TagHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagHolder =
        TagHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false)
        )

    override fun onBindViewHolder(holder: TagHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun TagHolder.bind(tag: Tag) = with(itemView) {
        chip.text = tag.name

        dialogState.observe(context as LifecycleOwner, Observer {
            if (it == DialogState.Edit)
                chip.isCloseIconVisible = true
            else if (it == DialogState.Normal)
                chip.isCloseIconVisible = false
        })
        chip.setOnCloseIconClickListener {
            onClick.invoke(tag, layoutPosition)
        }
    }

    class TagHolder(view: View) : RecyclerView.ViewHolder(view)
}