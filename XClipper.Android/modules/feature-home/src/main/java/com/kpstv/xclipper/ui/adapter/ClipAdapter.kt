package com.kpstv.xclipper.ui.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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

data class ClipAdapterItem constructor(val clip: Clip) {
    var expanded: Boolean = false

    companion object {
        fun from(clip: Clip) = ClipAdapterItem(clip = clip)
    }
}

class ClipAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val multiSelectionState: LiveData<Boolean>,
    private val selectedItem: LiveData<Clip>,
    private val currentClip: LiveData<String>,
    private val onClick: (Clip, Int) -> Unit,
    private val onLongClick: (Clip, Int) -> Unit,
    private val selectedClips: LiveData<List<Clip>>
) : ListAdapter<Clip, ClipAdapterHolder>(DiffCallback.asConfig(isBackground = true)) {

    private object DiffCallback : DiffUtil.ItemCallback<Clip>() {
        override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
            oldItem.data == newItem.data

        override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean = oldItem == newItem
    }

    private val TAG = javaClass.simpleName

    private lateinit var copyClick: (Clip, Int) -> Unit
    private lateinit var menuClick: (Clip, Int, MenuType) -> Unit

    private val selectedDataObservers = HashMap<Int, Observer<String>>()
    private val selectedItemObservers = HashMap<Int, Observer<Clip>>()
    private val multiSelectionObservers = HashMap<Int, Observer<Boolean>>()
    private val selectedClipsObservers = HashMap<Int, Observer<List<Clip>>>()
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
                    val clip = getItem(bindingAdapterPosition)
                    copyClick.invoke(clip, bindingAdapterPosition)
                }
                ciBtnEdit.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition)
                    menuClick.invoke(clip, bindingAdapterPosition, MenuType.Edit)
                }
                ciBtnPin.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition)
                    menuClick.invoke(clip, bindingAdapterPosition, MenuType.Pin)
                }
                ciBtnSpecial.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition)
                    menuClick.invoke(clip, bindingAdapterPosition, MenuType.Special)
                }
                ciBtnShare.setOnClickListener {
                    val clip = getItem(bindingAdapterPosition)
                    menuClick.invoke(clip, bindingAdapterPosition, MenuType.Share)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ClipAdapterHolder, position: Int) = with(holder) {
        val clip = getItem(position)

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

        val selectedDataObserver: Observer<String> = Observer { current ->
            applyForCurrentClipboardText(current)
        }
        val selectedItemObserver: Observer<Clip> = Observer { selectedClip ->
            applyForSelectedItem(clip, isExpanded = selectedClip == clip)
        }
        val multiSelectionObserver: Observer<Boolean> = Observer { state ->
            applyForMultiSelectionState(clip, isMultiSelectionState = state)
        }
        val selectedClipsObserver: Observer<List<Clip>> = Observer { clips ->
            applyForSelectedClips(clip, isExpanded = selectedItem.value == clip, clips)
        }

        // no need to remove these observers from hashmap as it will guarantee to not create duplicates causing memory leaks.

        currentClip.observe(lifecycleOwner, selectedDataObserver).also { selectedDataObservers[clip.id] = selectedDataObserver }
        selectedItem.observe(lifecycleOwner, selectedItemObserver).also { selectedItemObservers[clip.id] = selectedItemObserver }
        multiSelectionState.observe(lifecycleOwner, multiSelectionObserver).also { multiSelectionObservers[clip.id] = multiSelectionObserver }
        selectedClips.observe(lifecycleOwner, selectedClipsObserver).also { selectedClipsObservers[clip.id] = selectedClipsObserver }
    }

    fun updateSelectedItem(clip: Clip?) {
//        val position = currentList.i
    }

    fun setCopyClick(block: (Clip, Int) -> Unit) {
        this.copyClick = block
    }

    fun setMenuItemClick(block: (Clip, Int, MenuType) -> Unit) {
        this.menuClick = block
    }

    fun getItemAt(pos: Int): Clip = getItem(pos)

    enum class MenuType {
        Edit, Pin, Special, Share
    }
}

class ClipAdapterHolder(val binding: ItemClipBinding) : RecyclerView.ViewHolder(binding.root) {
    var tag: Tag = Tag()
    inner class Tag(var isSwipeEnabled: Boolean = true)

    private val context = binding.root.context

    fun applyForSelectedItem(clip: Clip, isExpanded: Boolean = false): Unit = with(binding) {
        updatePinButton(clip)
        if (isExpanded) {
            hiddenLayout.show()
            mainCard.setCardBackgroundColor(CARD_CLICK_COLOR)
            mainCard.cardElevation = context.toPx(3)
        } else {
            mainCard.setCardBackgroundColor(CARD_COLOR)
            mainCard.cardElevation = context.toPx(0)
            hiddenLayout.collapse()
        }
    }

    fun applyForCurrentClipboardText(current: String): Unit = with(binding) {
        if (ciTextView.text == current)
            ciTextView.setTextColor(
                context.getColorAttr(R.attr.colorCurrentClip)
            )
        else ciTextView.setTextColor(
            context.getColorAttr(R.attr.colorTextPrimary)
        )
    }

    fun applyForMultiSelectionState(clip: Clip, isMultiSelectionState: Boolean): Unit = with(binding) {
        if (isMultiSelectionState) {
            ciCopyButton.hide()
            ciTimeText.hide()
            ciTagLayout.hide()
            ciPinImage.hide()
        } else {
            ciCopyButton.show()
            ciTimeText.show()
            ciTagLayout.show()
            if (clip.isPinned) {
                ciPinImage.show()
            }
        }
    }

    fun applyForSelectedClips(clip: Clip, isExpanded: Boolean, selectedClips: List<Clip>?): Unit = with(binding) {
        if (selectedClips == null) return
        when {
            selectedClips.contains(clip) -> {
                mainCard.setCardBackgroundColor(CARD_SELECTED_COLOR)
            }
            else -> {
                /**
                * We are also checking if clip is expanded. Since for large item set
                * it kinda forgets about it due to recreation of whole list.
                */
                if (!isExpanded)
                    mainCard.setCardBackgroundColor(CARD_COLOR)
            }
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
        UpdateSelectedItem
    }
}

