package com.kpstv.xclipper.ui.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extension.drawableRes
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.feature_home.R
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

            val clipTag = tag.getClipTag()

            if (tag.type.isSpecialTag()) {
                chip.chipBackgroundColor = ColorStateList.valueOf(root.context.getColorAttr(R.attr.colorSpecialTag))
            } else {
                chip.chipBackgroundColor = ColorStateList.valueOf(root.context.getColorAttr(R.attr.colorTextSecondary))
            }

            chip.chipIconSize = root.context.toPx(20)
            chip.isCloseIconVisible = false
            chip.text = tag.name

            chip.setOnClickListener{ onClick.invoke(tag, layoutPosition) }

            selectedTags.observe(viewLifecycleOwner) { tagMap ->
                if (tagMap.containsKey(tag.name)) {
                    chip.chipIcon = root.context.drawableFrom(R.drawable.ic_check_circle)
                    chip.isChipIconVisible = true
                } else if (clipTag != null) {
                    chip.chipIcon = root.context.drawableFrom(clipTag.drawableRes)
                    chip.isChipIconVisible = true
                } else {
                    chip.isChipIconVisible = false
                }
            }
        }
    }
}