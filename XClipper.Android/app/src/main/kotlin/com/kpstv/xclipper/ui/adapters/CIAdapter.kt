package com.kpstv.xclipper.ui.adapters

import android.view.LayoutInflater
import android.view.View
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
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.converters.DateFormatConverter
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_CLICK_COLOR
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_COLOR
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_SELECTED_COLOR
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.getColorFromAttr
import kotlinx.android.synthetic.main.item_clip.view.*
import java.util.*
import kotlin.collections.HashMap

class CIAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val multiSelectionState: LiveData<Boolean>,
    private val selectedItem: LiveData<Clip>,
    private val currentClip: LiveData<String>,
    private val onClick: (Clip, Int) -> Unit,
    private val onLongClick: (Clip, Int) -> Unit,
    private val selectedClips: LiveData<List<Clip>>
) : ListAdapter<Clip, CIAdapter.MainHolder>(DiffCallback.asConfig()) {
    private object DiffCallback : DiffUtil.ItemCallback<Clip>() {
        override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
            oldItem.data == newItem.data

        override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean = oldItem == newItem
    }

    private val TAG = javaClass.simpleName

    private lateinit var copyClick: (Clip, Int) -> Unit
    private lateinit var menuClick: (Clip, Int, MENU_TYPE) -> Unit

    private val selectedDataObservers = HashMap<Int, Observer<String>>()
    private val selectedItemObservers = HashMap<Int, Observer<Clip>>()
    private val multiSelectionObservers = HashMap<Int, Observer<Boolean>>()
    private val selectedClipsObservers = HashMap<Int, Observer<List<Clip>>>()

    override fun getItemViewType(position: Int) = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder =
        MainHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_clip, parent, false)
        )

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val clip = getItem(position)

        holder.itemView.ci_textView.text = if (App.trimClipText) clip.data.trim() else clip.data
        holder.itemView.tag = clip.id // used for unsubscribing.

        if (clip.isPinned) {
            holder.itemView.ic_pinView.show()
        } else {
            holder.itemView.ic_pinView.hide()
        }

        if (App.LoadImageMarkdownText)
            renderImageMarkdown(holder, clip.data, position)

        holder.itemView.ci_timeText.text = DateFormatConverter.getFormattedDate(clip.time)

        setPinMovements(clip, holder)

        setTags(holder.itemView, clip)

        holder.itemView.mainCard.setOnClickListener { onClick.invoke(clip, position) }
        holder.itemView.mainCard.setOnLongClickListener {
            onLongClick.invoke(clip, position)
            true
        }
        holder.itemView.ci_copyButton.setOnClickListener { copyClick.invoke(clip, position) }
        holder.itemView.ci_btn_edit.setOnClickListener {
            menuClick.invoke(
                clip,
                position,
                MENU_TYPE.Edit
            )
        }
        holder.itemView.ci_btn_pin.setOnClickListener {
            menuClick.invoke(
                clip,
                position,
                MENU_TYPE.Pin
            )
        }
        holder.itemView.ci_btn_special.setOnClickListener {
            menuClick.invoke(
                clip,
                position,
                MENU_TYPE.Special
            )
        }
        holder.itemView.ci_btn_share.setOnClickListener {
            menuClick.invoke(
                clip,
                position,
                MENU_TYPE.Share
            )
        }

        val selectedDataObserver: Observer<String> = Observer { current ->
            if (holder.itemView.ci_textView.text == current)
                holder.itemView.ci_textView.setTextColor(
                    getColorFromAttr(holder.itemView.context, R.attr.colorCurrentClip)
                )
            else holder.itemView.ci_textView.setTextColor(
                getColorFromAttr(holder.itemView.context, R.attr.colorTextPrimary)
            )
        }
        val selectedItemObserver: Observer<Clip> = Observer { selectedClip ->
            setPinMovements(clip, holder)
            if (selectedClip == clip) {
                holder.itemView.hiddenLayout.show()
                holder.itemView.mainCard.setCardBackgroundColor(CARD_CLICK_COLOR)
                holder.itemView.mainCard.cardElevation = Utils.dpToPixel(holder.itemView.context, 3f)
            } else {
                holder.itemView.mainCard.setCardBackgroundColor(CARD_COLOR)
                holder.itemView.mainCard.cardElevation = Utils.dpToPixel(holder.itemView.context, 0f)
                holder.itemView.hiddenLayout.collapse()
            }
        }
        val multiSelectionObserver: Observer<Boolean> = Observer { state ->
            if (state) {
                holder.itemView.ci_copyButton.hide()
                holder.itemView.ci_timeText.hide()
                holder.itemView.ci_tagLayout.hide()
            } else {
                holder.itemView.ci_copyButton.show()
                holder.itemView.ci_timeText.show()
                holder.itemView.ci_tagLayout.show()
            }
        }
        val selectedClipsObserver: Observer<List<Clip>> = Observer { clips ->
            if (clips == null) return@Observer
            when {
                clips.contains(clip) -> {
                    holder.itemView.mainCard.setCardBackgroundColor(CARD_SELECTED_COLOR)
                }
                else -> {
                    /**
                     * We are also checking if selected item is this clip. Since for large item set
                     * it kinda forgets about it due to recreation of whole list.
                     */
                    if (selectedItem.value != clip)
                        holder.itemView.mainCard.setCardBackgroundColor(CARD_COLOR)
                }
            }
        }

        // no need to remove these observers from hashmap as it will guarantee to not create duplicates causing memory leaks.

        currentClip.observe(lifecycleOwner, selectedDataObserver).also { selectedDataObservers[clip.id] = selectedDataObserver }
        selectedItem.observe(lifecycleOwner, selectedItemObserver).also { selectedItemObservers[clip.id] = selectedItemObserver }
        multiSelectionState.observe(lifecycleOwner, multiSelectionObserver).also { multiSelectionObservers[clip.id] = multiSelectionObserver }
        selectedClips.observe(lifecycleOwner, selectedClipsObserver).also { selectedClipsObservers[clip.id] = selectedClipsObserver }
    }

    private fun renderImageMarkdown(holder: MainHolder, data: String?, position: Int) {
        val result = App.MARKDOWN_IMAGE_ONLY_REGEX.toRegex().matchEntire(data ?: "")
        if (result != null) {
            val imageUrl = result.groups[5]?.value

            holder.itemView.ci_imageView.show()

            holder.itemView.ci_imageView.load(
                uri = imageUrl,
                onSuccess = {
                    holder.itemView.ci_textView.hide()
                },
                onError = {
                    holder.itemView.ci_imageView.collapse()
                    holder.itemView.ci_textView.show()
                }
            )
        } else {
            holder.itemView.ci_textView.show()
            holder.itemView.ci_imageView.collapse()
        }
    }

    private fun setPinMovements(
        clip: Clip,
        holder: MainHolder
    ) {
        if (clip.isPinned) {
            setButtonDrawable(
                holder.itemView,
                R.drawable.ic_unpin
            )
            holder.itemView.ci_btn_pin.text = holder.itemView.context.getString(R.string.unpin)
            holder.itemView.ci_pinImage.show()
        } else {
            setButtonDrawable(
                holder.itemView,
                R.drawable.ic_pin
            )
            holder.itemView.ci_btn_pin.text = holder.itemView.context.getString(R.string.pin)
            holder.itemView.ci_pinImage.collapse()
        }
    }

    private fun setButtonDrawable(view: View, @DrawableRes imageId: Int) {
        view.ci_btn_pin.setCompoundDrawablesWithIntrinsicBounds(
            null,
            ContextCompat.getDrawable(view.ci_btn_pin.context, imageId),
            null, null
        )
    }

    private fun setTags(view: View, clip: Clip) {
        view.ci_tagLayout.removeAllViews()
        clip.tags?.keys()?.forEach mainLoop@{ key ->
            if (key.isNotBlank()) {
                val textView = LayoutInflater.from(view.ci_btn_pin.context)
                    .inflate(R.layout.item_tag, null) as TextView
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

                view.ci_tagLayout.addView(textView)
            }
        }
    }

    fun setCopyClick(block: (Clip, Int) -> Unit) {
        this.copyClick = block
    }

    fun setMenuItemClick(block: (Clip, Int, MENU_TYPE) -> Unit) {
        this.menuClick = block
    }

    fun getItemAt(pos: Int): Clip =
        getItem(pos)

    enum class MENU_TYPE {
        Edit, Pin, Special, Share
    }

    class MainHolder(view: View) : RecyclerView.ViewHolder(view)
}

