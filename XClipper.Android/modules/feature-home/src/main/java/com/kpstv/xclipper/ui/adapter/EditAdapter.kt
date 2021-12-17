package com.kpstv.xclipper.ui.adapter

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.extensions.ClipTagMap
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.containsKey
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.feature_home.databinding.ItemTagChipBinding

class EditAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val selectedTags: LiveData<List<ClipTagMap>>,
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
        EditHolder(ItemTagChipBinding.inflate(parent.context.layoutInflater(), parent, false))

    override fun onBindViewHolder(holder: EditHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EditHolder(private val binding: ItemTagChipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: Tag) = with(binding) {
            chip.isCloseIconVisible = false
            chip.text = tag.name

            chip.setOnClickListener{ onClick.invoke(tag, layoutPosition) }

            selectedTags.observe(viewLifecycleOwner) {
                chip.isChipIconVisible = it?.containsKey(tag.name) == true
            }
        }
    }
}