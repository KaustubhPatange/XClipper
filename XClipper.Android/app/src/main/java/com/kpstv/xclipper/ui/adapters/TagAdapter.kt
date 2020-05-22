package com.kpstv.xclipper.ui.adapters

import android.util.Log
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
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.Tag
import kotlinx.android.synthetic.main.tag_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class TagAdapter(
    private val dialogState: LiveData<DialogState>,
    private val tagFilter: LiveData<ArrayList<Tag>>,
    private val onCloseClick: (Tag, Int) -> Unit,
    private val onClick: (Tag, Int) -> Unit
) : ListAdapter<Tag, TagAdapter.TagHolder>(DiffCallback()) {

    private val TAG = javaClass.simpleName

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
            if (it == DialogState.Edit) {
                if (ClipTag.fromValue(tag.name.toUpperCase(Locale.ROOT)) == null)
                    chip.isCloseIconVisible = true
            }
            else if (it == DialogState.Normal)
                chip.isCloseIconVisible = false
        })

        tagFilter.observe(context as LifecycleOwner, Observer {
            chip.isChipIconVisible = it.contains(tag)
        })

        chip.setOnCloseIconClickListener {
            onCloseClick.invoke(tag, layoutPosition)
        }
        chip.setOnClickListener { onClick.invoke(tag, layoutPosition) }
    }

    class TagHolder(view: View) : RecyclerView.ViewHolder(view)
}