package com.kpstv.xclipper.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.model.TagMap
import com.kpstv.xclipper.extension.enumeration.DialogState
import com.kpstv.xclipper.extension.titleRes
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.feature_home.databinding.ItemTagChipBinding

class TagAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val dialogState: LiveData<DialogState>,
    private val tagFilter: LiveData<List<Tag>>,
    private val tagMapData: LiveData<List<TagMap>>,
    private val onCloseClick: (Tag, count: Int, index: Int) -> Unit,
    private val onClick: (Tag, Int) -> Unit
) : ListAdapter<Tag, TagAdapter.TagHolder>(DiffCallback()) {

    private val TAG = javaClass.simpleName

    class DiffCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagHolder =
        TagHolder(ItemTagChipBinding.inflate(parent.context.layoutInflater(), parent, false))

    override fun onBindViewHolder(holder: TagHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class TagHolder(private val binding: ItemTagChipBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(tag: Tag) = with(binding) {
            val clipTag = tag.getClipTag()
            if (!tag.type.isUserTag() && clipTag != null) {
                chip.text = root.context.getString(clipTag.titleRes)
            } else {
                chip.text = tag.name
            }

            dialogState.observe(lifecycleOwner) {
                if (it == DialogState.Edit) {
                    chip.isCloseIconVisible = clipTag == null
                }
                else if (it == DialogState.Normal)
                    chip.isCloseIconVisible = false
            }

            tagMapData.observe(lifecycleOwner) { list ->
                val find = list.find { it.name == tag.name }
                if (find?.count != null) {
                    chip.text = "${tag.name} (${find.count})"
                    chip.tag = find.count
                }
            }

            tagFilter.observe(lifecycleOwner) { list ->
                chip.isChipIconVisible = list.any { it.name == tag.name }
            }

            chip.setOnCloseIconClickListener { v ->
                onCloseClick.invoke(tag, v.tag as? Int ?: 0, layoutPosition)
            }
            chip.setOnClickListener { onClick.invoke(tag, layoutPosition) }
        }
    }
}