package com.kpstv.xclipper.ui.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.kpstv.xclipper.data.converters.DateFormatConverter
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.utils.ClipUtils
import com.kpstv.xclipper.feature_home.R
import com.kpstv.xclipper.feature_home.databinding.ItemClipBinding
import com.kpstv.xclipper.ui.helpers.AppThemeHelper.CARD_CLICK_COLOR
import com.kpstv.xclipper.ui.helpers.AppThemeHelper.CARD_COLOR
import com.kpstv.xclipper.ui.helpers.AppThemeHelper.CARD_SELECTED_COLOR
import okhttp3.internal.filterList

data class ClipAdapterItem constructor(
    val clip: Clip,
    var expanded: Boolean = false,
    var selected: Boolean = false,
    var selectedClipboard: Boolean = false,
    var multiSelectionState: Boolean = false
) {
    companion object {
        fun from(clip: Clip) = ClipAdapterItem(clip = clip)

        fun List<ClipAdapterItem>.toClips(): List<Clip> = map { it.clip }
    }
}

class ClipAdapter(
    private val onClick: (ClipAdapterItem, Int) -> Unit,
    private val onLongClick: (ClipAdapterItem, Int) -> Unit,
) : ListAdapter<ClipAdapterItem, ClipAdapterHolder>(DiffCallback.asConfig(isBackground = true)) {

    private object DiffCallback : DiffUtil.ItemCallback<ClipAdapterItem>() {
        override fun areItemsTheSame(oldItem: ClipAdapterItem, newItem: ClipAdapterItem): Boolean =
            oldItem.clip.data == newItem.clip.data

        override fun areContentsTheSame(oldItem: ClipAdapterItem, newItem: ClipAdapterItem): Boolean = oldItem.clip == newItem.clip
    }

    private val TAG = javaClass.simpleName

    private lateinit var copyClick: (Clip, Int) -> Unit
    private lateinit var menuClick: (Clip, Int, MenuType) -> Unit

    private var trimClipText : Boolean = false
    private var loadImageMarkdownText : Boolean = true

    fun setTextTrimmingEnabled(value : Boolean) {
        trimClipText = value
    }

    fun setIsLoadingMarkdownEnabled(value: Boolean) {
        loadImageMarkdownText = value
    }

    override fun getItemViewType(position: Int) = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipAdapterHolder {
        return ClipAdapterHolder(ItemClipBinding.inflate(parent.context.layoutInflater(), parent, false)).apply {
            with(binding) {
                mainCard.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition)
                    onClick.invoke(clip, bindingAdapterPosition)
                }
                mainCard.setOnLongClickListener {
                    val clip = getItem(bindingAdapterPosition)
                    onLongClick.invoke(clip, bindingAdapterPosition)
                    true
                }
                ciCopyButton.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition).clip
                    copyClick.invoke(clip, bindingAdapterPosition)
                }
                ciBtnEdit.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition).clip
                    menuClick.invoke(clip, bindingAdapterPosition, MenuType.Edit)
                }
                ciBtnPin.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition).clip
                    menuClick.invoke(clip, bindingAdapterPosition, MenuType.Pin)
                }
                ciBtnSpecial.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition).clip
                    menuClick.invoke(clip, bindingAdapterPosition, MenuType.Special)
                }
                ciBtnShare.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition).clip
                    menuClick.invoke(clip, bindingAdapterPosition, MenuType.Share)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ClipAdapterHolder, position: Int, payloads: MutableList<Any>) {
        val clipAdapterItem = getItem(position)
        for (payload in payloads) {
            when(payload) {
                ClipAdapterHolder.Payloads.UpdateExpandedState -> holder.applyForExpandedItem(clipAdapterItem)
                ClipAdapterHolder.Payloads.UpdateSelectedState -> holder.applyForSelectedItem(clipAdapterItem)
                ClipAdapterHolder.Payloads.UpdateMultiSelectionState -> holder.applyForMultiSelectionState(clipAdapterItem)
                ClipAdapterHolder.Payloads.UpdateCurrentClipboardText -> holder.applyForCurrentClipboardText(clipAdapterItem)
            }
        }
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: ClipAdapterHolder, position: Int) = with(holder) {
        val clipAdapterItem = getItem(position)
        val clip = clipAdapterItem.clip

        binding.ciTextView.text = if (trimClipText) clip.data.trim() else clip.data
        binding.root.tag = clip.id // used for unsubscribing.

        if (clip.isPinned) {
            binding.icPinView.show()
        } else {
            binding.icPinView.hide()
        }

        if (loadImageMarkdownText) {
            renderImageMarkdown(clip.data)
        }

        binding.ciTimeText.text = DateFormatConverter.getFormattedDate(clip.time)

        updatePinButton(clip)

        updateTags(clip)
        updateHolderTags(clip)

        applyForCurrentClipboardText(clipAdapterItem)
        applyForExpandedItem(clipAdapterItem)
        applyForMultiSelectionState(clipAdapterItem)
        applyForSelectedItem(clipAdapterItem)
    }

    fun updateItemsForMultiSelectionState(isMultiSelectionState: Boolean) {
        for (item in currentList) {
            item.multiSelectionState = isMultiSelectionState
        }
        notifyItemRangeChanged(0, currentList.size, ClipAdapterHolder.Payloads.UpdateMultiSelectionState)
    }

    fun updateCurrentClipboardItem(text: String?) {
        val currentClipboardItem = currentList.firstOrNull { it.selectedClipboard }
        if (text == null || currentClipboardItem?.clip?.data != text) {
            currentClipboardItem?.let { item ->
                val position = currentList.indexOf(item)
                item.selectedClipboard = false
                notifyItemChanged(position, ClipAdapterHolder.Payloads.UpdateCurrentClipboardText)
            }
        }

        if (text != null) {
            val item = currentList.firstOrNull { it.clip.data == text } ?: return
            val position = currentList.indexOf(item)
            item.selectedClipboard = true
            notifyItemChanged(position, ClipAdapterHolder.Payloads.UpdateCurrentClipboardText)
        }
    }

    fun updateExpandedItem(clip: ClipAdapterItem) {
        val currentExpandedItemPosition = currentList.indexOfFirst { it.expanded }
        val position = currentList.indexOf(clip)

        if (currentExpandedItemPosition != -1 && currentExpandedItemPosition != position) {
            clearExpandedItem()
        }

        clip.expanded = !clip.expanded
        notifyItemChanged(position, ClipAdapterHolder.Payloads.UpdateExpandedState)
    }

    fun clearExpandedItem() {
        val clipAdapterItem = currentList.firstOrNull { it.expanded } ?: return
        val position = currentList.indexOf(clipAdapterItem)
        clipAdapterItem.expanded = false
        notifyItemChanged(position)
    }

    fun addToSelectionItems(clips: List<ClipAdapterItem>) {
        for(item in currentList) {
            item.selected = false
        }
        for(item in clips) {
            item.selected = true
        }
        notifyItemRangeChanged(0, currentList.size, ClipAdapterHolder.Payloads.UpdateSelectedState)
    }

    fun updateAllItemsToSelectedState() {
        updateSelectionStateForAllItems(isSelected = true)
    }

    fun clearAllSelectedItems() {
        updateSelectionStateForAllItems(isSelected = false)
    }
    private fun updateSelectionStateForAllItems(isSelected: Boolean) {
        for(item in currentList) {
            item.selected = isSelected
        }
        notifyItemRangeChanged(0, currentList.size, ClipAdapterHolder.Payloads.UpdateSelectedState)
    }

    fun setCopyClick(block: (Clip, Int) -> Unit) {
        this.copyClick = block
    }

    fun setMenuItemClick(block: (Clip, Int, MenuType) -> Unit) {
        this.menuClick = block
    }

    fun getItemAt(pos: Int): ClipAdapterItem = getItem(pos)

    enum class MenuType {
        Edit, Pin, Special, Share
    }
}

class ClipAdapterHolder(val binding: ItemClipBinding) : RecyclerView.ViewHolder(binding.root) {
    var tag: Tag = Tag()
    inner class Tag(var isSwipeEnabled: Boolean = true)

    private val context = binding.root.context

    fun applyForExpandedItem(clipAdapterItem: ClipAdapterItem): Unit = with(binding) {
        updatePinButton(clipAdapterItem.clip)
        if (clipAdapterItem.expanded) {
            hiddenLayout.show()
            mainCard.setCardBackgroundColor(CARD_CLICK_COLOR)
            mainCard.cardElevation = context.toPx(3)
        } else {
            mainCard.setCardBackgroundColor(CARD_COLOR)
            mainCard.cardElevation = context.toPx(0)
            hiddenLayout.collapse()
        }
    }

    fun applyForCurrentClipboardText(clipAdapterItem: ClipAdapterItem): Unit = with(binding) {
        if (clipAdapterItem.selectedClipboard) {
            ciTextView.setTextColor(context.getColorAttr(R.attr.colorCurrentClip))
        } else {
            ciTextView.setTextColor(context.getColorAttr(R.attr.colorTextPrimary))
        }
    }

    fun applyForMultiSelectionState(clipAdapterItem: ClipAdapterItem): Unit = with(binding) {
        if (clipAdapterItem.multiSelectionState) {
            ciCopyButton.hide()
            ciTimeText.hide()
            ciTagLayout.hide()
            ciPinImage.hide()
        } else {
            ciCopyButton.show()
            ciTimeText.show()
            ciTagLayout.show()
            if (clipAdapterItem.clip.isPinned) {
                ciPinImage.show()
            }
        }
    }

    fun applyForSelectedItem(clipAdapterItem: ClipAdapterItem): Unit = with(binding) {
        if (clipAdapterItem.selected) {
            mainCard.setCardBackgroundColor(CARD_SELECTED_COLOR)
        } else if (!clipAdapterItem.expanded) {
            mainCard.setCardBackgroundColor(CARD_COLOR)
        }
    }

    fun updatePinButton(clip: Clip): Unit = with(binding) {
        if (clip.isPinned) {
            setButtonDrawable(ciBtnPin, R.drawable.ic_unpin)
            ciBtnPin.text = context.getString(R.string.unpin)
            ciPinImage.show()
        } else {
            setButtonDrawable(ciBtnPin, R.drawable.ic_pin)
            ciBtnPin.text = context.getString(R.string.pin)
            ciPinImage.collapse()
        }
    }
    private fun setButtonDrawable(view: TextView, @DrawableRes imageId: Int) {
        view.setCompoundDrawablesWithIntrinsicBounds(
            null, ContextCompat.getDrawable(view.context, imageId), null, null
        )
    }

    fun updateTags(clip: Clip): Unit = with(binding) {
        ciTagLayout.removeAllViews()
        clip.tags?.keys()?.forEach mainLoop@{ key ->
            if (key.isNotBlank()) {
                val textView = root.context.layoutInflater().inflate(R.layout.item_tag, null) as TextView
                val layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )

                layoutParams.topMargin = 2
                layoutParams.bottomMargin = 2
                layoutParams.marginEnd = 5
                layoutParams.marginStart = 5
                textView.layoutParams = layoutParams
                textView.text = key

                ciTagLayout.addView(textView)
            }
        }
    }

    fun renderImageMarkdown(data: String) : Unit = with(binding) {
        if (ClipUtils.isMarkdownImage(data)) {
            val imageUrl = ClipUtils.getMarkdownImageUrl(data)

            ciImageView.show()

            ciImageView.load(
                uri = imageUrl,
                onSuccess = {
                    ciTextView.hide()
                },
                onError = {
                    ciImageView.collapse()
                    ciTextView.show()
                }
            )
        } else {
            ciTextView.show()
            ciImageView.collapse()
        }
    }

    fun updateHolderTags(clip: Clip) {
        tag.isSwipeEnabled = (clip.tags?.none { it.key == ClipTag.LOCK.small() } == true)
    }

    enum class Payloads {
        UpdateExpandedState,
        UpdateSelectedState,
        UpdateMultiSelectionState,
        UpdateCurrentClipboardText
    }
}

