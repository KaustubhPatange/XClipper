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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipAdapterHolder =
        ClipAdapterHolder(ItemClipBinding.inflate(parent.context.layoutInflater(), parent, false))

    override fun onBindViewHolder(holder: ClipAdapterHolder, position: Int) = with(holder.binding) {
        val clip = getItem(position)

        ciTextView.text = if (trimClipText) clip.data.trim() else clip.data
        root.tag = clip.id // used for unsubscribing.

        if (clip.isPinned) {
            icPinView.show()
        } else {
            icPinView.hide()
        }

        if (loadImageMarkdownText) {
            renderImageMarkdown(holder, clip.data)
        }

        ciTimeText.text = DateFormatConverter.getFormattedDate(clip.time)

        setPinMovements(clip, holder)

        setTags(holder, clip)
        setHolderTagData(holder, clip)

        mainCard.setOnClickListener { onClick.invoke(clip, position) }
        mainCard.setOnLongClickListener {
            onLongClick.invoke(clip, position)
            true
        }
        ciCopyButton.setOnClickListener { copyClick.invoke(clip, position) }
        ciBtnEdit.setOnClickListener {
            menuClick.invoke(
                clip,
                position,
                MenuType.Edit
            )
        }
        ciBtnPin.setOnClickListener {
            menuClick.invoke(
                clip,
                position,
                MenuType.Pin
            )
        }
        ciBtnSpecial.setOnClickListener {
            menuClick.invoke(
                clip,
                position,
                MenuType.Special
            )
        }
        ciBtnShare.setOnClickListener {
            menuClick.invoke(
                clip,
                position,
                MenuType.Share
            )
        }

        val selectedDataObserver: Observer<String> = Observer { current ->
            if (ciTextView.text == current)
                ciTextView.setTextColor(
                    holder.itemView.context.getColorAttr(R.attr.colorCurrentClip)
                )
            else ciTextView.setTextColor(
                holder.itemView.context.getColorAttr(R.attr.colorTextPrimary)
            )
        }
        val selectedItemObserver: Observer<Clip> = Observer { selectedClip ->
            setPinMovements(clip, holder)
            if (selectedClip == clip) {
                hiddenLayout.show()
                mainCard.setCardBackgroundColor(CARD_CLICK_COLOR)
                mainCard.cardElevation = holder.itemView.context.toPx(3)
            } else {
                mainCard.setCardBackgroundColor(CARD_COLOR)
                mainCard.cardElevation = holder.itemView.context.toPx(0)
                hiddenLayout.collapse()
            }
        }
        val multiSelectionObserver: Observer<Boolean> = Observer { state ->
            if (state) {
                ciCopyButton.hide()
                ciTimeText.hide()
                ciTagLayout.hide()
            } else {
                ciCopyButton.show()
                ciTimeText.show()
                ciTagLayout.show()
            }
        }
        val selectedClipsObserver: Observer<List<Clip>> = Observer { clips ->
            if (clips == null) return@Observer
            when {
                clips.contains(clip) -> {
                    mainCard.setCardBackgroundColor(CARD_SELECTED_COLOR)
                }
                else -> {
                    /**
                     * We are also checking if selected item is this clip. Since for large item set
                     * it kinda forgets about it due to recreation of whole list.
                     */
                    /**
                     * We are also checking if selected item is this clip. Since for large item set
                     * it kinda forgets about it due to recreation of whole list.
                     */
                    if (selectedItem.value != clip)
                        mainCard.setCardBackgroundColor(CARD_COLOR)
                }
            }
        }

        // no need to remove these observers from hashmap as it will guarantee to not create duplicates causing memory leaks.

        currentClip.observe(lifecycleOwner, selectedDataObserver).also { selectedDataObservers[clip.id] = selectedDataObserver }
        selectedItem.observe(lifecycleOwner, selectedItemObserver).also { selectedItemObservers[clip.id] = selectedItemObserver }
        multiSelectionState.observe(lifecycleOwner, multiSelectionObserver).also { multiSelectionObservers[clip.id] = multiSelectionObserver }
        selectedClips.observe(lifecycleOwner, selectedClipsObserver).also { selectedClipsObservers[clip.id] = selectedClipsObserver }
    }

    private fun setHolderTagData(holder: ClipAdapterHolder, clip: Clip) {
        holder.tag.isSwipeEnabled = (clip.tags?.none { it.key == ClipTag.LOCK.small() } == true)
    }

    private fun renderImageMarkdown(holder: ClipAdapterHolder, data: String) : Unit = with(holder.binding) {
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

    private fun setPinMovements(
        clip: Clip,
        holder: ClipAdapterHolder
    ) = with(holder.binding) {
        if (clip.isPinned) {
            setButtonDrawable(ciBtnPin, R.drawable.ic_unpin)
            ciBtnPin.text = holder.itemView.context.getString(R.string.unpin)
            ciPinImage.show()
        } else {
            setButtonDrawable(ciBtnPin, R.drawable.ic_pin)
            ciBtnPin.text = holder.itemView.context.getString(R.string.pin)
            ciPinImage.collapse()
        }
    }

    private fun setButtonDrawable(view: TextView, @DrawableRes imageId: Int) {
        view.setCompoundDrawablesWithIntrinsicBounds(
            null, ContextCompat.getDrawable(view.context, imageId), null, null
        )
    }

    private fun setTags(holder: ClipAdapterHolder, clip: Clip) = with(holder.binding) {
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

    fun setCopyClick(block: (Clip, Int) -> Unit) {
        this.copyClick = block
    }

    fun setMenuItemClick(block: (Clip, Int, MenuType) -> Unit) {
        this.menuClick = block
    }

    fun getItemAt(pos: Int): Clip =
        getItem(pos)

    enum class MenuType {
        Edit, Pin, Special, Share
    }
}

class ClipAdapterHolder(val binding: ItemClipBinding) : RecyclerView.ViewHolder(binding.root) {
    var tag: Tag = Tag()
    inner class Tag(var isSwipeEnabled: Boolean = true)
}

