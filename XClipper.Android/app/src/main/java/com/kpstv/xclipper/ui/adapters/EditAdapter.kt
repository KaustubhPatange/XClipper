package com.kpstv.xclipper.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Tag
import kotlinx.android.synthetic.main.tag_item.view.*
import org.kodein.di.android.x.AndroidLifecycleScope


class EditAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val selectedTags: LiveData<Map<String, String>>,
    private val onClick: (Tag, Int) -> Unit
) : ListAdapter<Tag, EditAdapter.EditHolder>(DiffCallback()) {

    private val TAG = javaClass.simpleName

    class DiffCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag) =
            oldItem.id == newItem.id

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
        chip.isCloseIconVisible = false
        chip.text = tag.name
/*
        chip.isChipIconVisible = clip.tags?.keys?.count { it == tag.name }!! > 0*/

        chip.setOnClickListener{ onClick.invoke(tag, layoutPosition) }

        selectedTags.observe(viewLifecycleOwner, Observer {
            chip.isChipIconVisible = it?.containsKey(tag.name) == true
        })
    }

    class EditHolder(view: View) : RecyclerView.ViewHolder(view)
}