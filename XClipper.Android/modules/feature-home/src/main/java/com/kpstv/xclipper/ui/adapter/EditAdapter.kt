package com.kpstv.xclipper.ui.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extension.drawableRes
import com.kpstv.xclipper.extension.titleRes
import com.kpstv.xclipper.extension.tooltipRes
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.feature_home.R
import com.kpstv.xclipper.feature_home.databinding.ItemTagChipBinding
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.createBalloon

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : EditHolder {
        val binding = ItemTagChipBinding.inflate(parent.context.layoutInflater(), parent, false)
        val holder = EditHolder(binding)
        with(binding) {
            chip.chipIconSize = root.context.toPx(20)
            chip.isCloseIconVisible = false
            chip.setOnClickListener { onClick(this@EditAdapter.getItem(holder.bindingAdapterPosition), holder.bindingAdapterPosition) }
            chip.setOnLongClickListener {
                val clipTag = this@EditAdapter.getItem(holder.bindingAdapterPosition).getClipTag()
                if (clipTag != null) {
                    createBalloon(root.context) {
                        text = root.context.getString(clipTag.tooltipRes)
                        setLifecycleOwner(viewLifecycleOwner)
                        setPadding(10)
                        arrowPositionRules = ArrowPositionRules.ALIGN_ANCHOR
                        arrowOrientation = ArrowOrientation.BOTTOM
                        backgroundColor = root.context.getColorAttr(R.attr.colorSpecialTag)
                        textColor = root.context.getColorAttr(R.attr.colorForeground)
                        dismissWhenTouchOutside = true
                    }.showAlignBottom(chip)
                }
                return@setOnLongClickListener clipTag != null
            }
        }
        return holder
    }

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

            if (clipTag != null) {
                chip.text = root.context.getString(clipTag.titleRes)
            } else {
                chip.text = tag.name
            }

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