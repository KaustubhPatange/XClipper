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
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_CLICK_COLOR
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_COLOR
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_SELECTED_COLOR
import com.kpstv.xclipper.extensions.utils.Utils.Companion.getColorFromAttr
import com.kpstv.yts.extensions.load
import kotlinx.android.synthetic.main.item_clip.view.*
import java.util.*

class CIAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val multiSelectionState: LiveData<Boolean>,
    private val selectedItem: LiveData<Clip>,
    private val currentClip: LiveData<String>,
    private val onClick: (Clip, Int) -> Unit,
    private val onLongClick: (Clip, Int) -> Unit,
    private val selectedClips: LiveData<ArrayList<Clip>>
) : ListAdapter<Clip, CIAdapter.MainHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<Clip>() {
        override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
            oldItem.data == newItem.data

        override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean = oldItem == newItem
    }

    private val TAG = javaClass.simpleName

    private lateinit var copyClick: (Clip, Int) -> Unit
    private lateinit var menuClick: (Clip, Int, MENU_TYPE) -> Unit

    override fun getItemViewType(position: Int) = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder =
        MainHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_clip, parent, false)
        )

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val clip = getItem(position)

        holder.itemView.ci_textView.text = clip.data

        renderImageMarkdown(holder, clip.data)

        holder.itemView.ci_timeText.text = clip.timeString

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

        currentClip.observe(lifecycleOwner, Observer {
            if (holder.itemView.ci_textView.text == it)
                holder.itemView.ci_textView.setTextColor(
                    getColorFromAttr(holder.itemView.context, R.attr.colorCurrentClip)
                )
            else holder.itemView.ci_textView.setTextColor(
                getColorFromAttr(holder.itemView.context, R.attr.colorTextPrimary)
            )
        })

        selectedItem.observe(lifecycleOwner, Observer {

            /** Will figure out where to place this */
            setPinMovements(clip, holder)

            if (it == clip) {
                holder.itemView.hiddenLayout.show()
                holder.itemView.mainCard.setCardBackgroundColor(CARD_CLICK_COLOR)
            } else {
                holder.itemView.mainCard.setCardBackgroundColor(CARD_COLOR)
                holder.itemView.hiddenLayout.collapse()
            }
        })

        multiSelectionState.observe(lifecycleOwner, Observer {
            if (it) {
                holder.itemView.ci_copyButton.hide()
                holder.itemView.ci_timeText.hide()
                holder.itemView.ci_tagLayout.hide()
            } else {
                holder.itemView.ci_copyButton.show()
                holder.itemView.ci_timeText.show()
                holder.itemView.ci_tagLayout.show()
            }
        })

        selectedClips.observe(lifecycleOwner, Observer {
            when {
                it.contains(clip) -> {
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
        })
    }

    private fun renderImageMarkdown(holder: MainHolder, data: String?) {
        val result = App.MARKDOWN_IMAGE_ONLY_REGEX.toRegex().matchEntire(data ?: "")
        if (result != null) {
            val imageUrl = result.groups[5]?.value

            holder.itemView.ci_imageView.show()
            //holder.itemView.ci_imageView.load(imageUrl)

            holder.itemView.ci_imageView.load(
                uri = imageUrl,
                onSuccess = {
                   // holder.itemView.ci_textView.hide()
                },
                onError = {
                    holder.itemView.ci_imageView.collapse()
                   // holder.itemView.ci_textView.show()
                }
            )

//            val request = ImageRequest.Builder(context)
//                .data(imageUrl)
//                .target(holder.itemView.ci_imageView)
//                .listener(
//                    onSuccess = { _, _ ->
//                        holder.itemView.ci_textView.hide()
//                    },
//                    onError = { _, _ ->
//                        holder.itemView.ci_imageView.collapse()
//                        holder.itemView.ci_textView.show()
//                    }
//                ).build()
//            Coil.enqueue(request)
        } else {
          //  holder.itemView.ci_textView.show()
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
        clip.tags?.forEach mainLoop@{ entry ->
            if (entry.key.isNotBlank()) {
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
                textView.text = entry.key

                view.ci_tagLayout.addView(textView)
            }
        }
    }

    fun setCopyClick(block: (Clip, Int) -> Unit) {
        this.copyClick = block;
    }

    fun setMenuItemClick(block: (Clip, Int, MENU_TYPE) -> Unit) {
        this.menuClick = block;
    }

    fun getItemAt(pos: Int): Clip =
        getItem(pos)

    enum class MENU_TYPE {
        Edit, Pin, Special, Share
    }

    class MainHolder(view: View) : RecyclerView.ViewHolder(view)
}

