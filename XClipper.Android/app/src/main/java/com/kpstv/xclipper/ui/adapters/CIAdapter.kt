package com.kpstv.xclipper.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
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
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.flexbox.FlexboxLayout
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_COLOR
import com.kpstv.xclipper.extensions.utils.ThemeUtils.Companion.CARD_SELECTED_COLOR
import com.kpstv.xclipper.extensions.utils.Utils
import kotlinx.android.synthetic.main.content_item.view.*
import java.util.*


class CIAdapter(
    private val context: Context,
    private val multiSelectionState: LiveData<Boolean>,
    private val selectedItem: LiveData<Clip>,
    private val currentClip: LiveData<String>,
    private val onClick: (Clip, Int) -> Unit,
    private val onLongClick: (Clip, Int) -> Unit,
    private val selectedClips: LiveData<ArrayList<Clip>>
) : ListAdapter<Clip, CIAdapter.MainHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<Clip>() {
        override fun areItemsTheSame(oldItem: Clip, newItem: Clip): Boolean =
            oldItem.data?.Decrypt() == newItem.data?.Decrypt()

        override fun areContentsTheSame(oldItem: Clip, newItem: Clip): Boolean = oldItem == newItem
    }

    private val TAG = javaClass.simpleName

    private lateinit var copyClick: (Clip, Int) -> Unit
    private lateinit var menuClick: (Clip, Int, MENU_TYPE) -> Unit


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder =
        MainHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.content_item, parent, false)
        )

    override fun onBindViewHolder(holder: MainHolder, position: Int) {

        val clip = getItem(position)

        holder.itemView.ci_textView.text = clip.data?.Decrypt()

        holder.itemView.mainCard.setOnClickListener { onClick.invoke(clip, position) }

        holder.itemView.ci_timeText.text = clip.timeString

        setPinMovements(clip, holder)

        setTags(holder.itemView, clip)

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

        currentClip.observe(context as LifecycleOwner, Observer {
            if (holder.itemView.ci_textView.text == it)
                holder.itemView.ci_textView.setTextColor(
                    ContextCompat.getColor(context, R.color.colorSelectedClip)
                )
            else holder.itemView.ci_textView.setTextColor(
                Utils.getColorFromAttr(context, R.attr.colorTextPrimary)
            )
        })

        selectedItem.observe(context as LifecycleOwner, Observer {


            /** Will figure out where to place this */
            setPinMovements(clip, holder)

            if (it == clip) {
                holder.itemView.hiddenLayout.show()
                holder.itemView.mainCard.setCardBackgroundColor(CARD_COLOR)
            } else {
                holder.itemView.mainCard.setCardBackgroundColor(Color.TRANSPARENT)
                holder.itemView.hiddenLayout.visibility = View.GONE
            }
        })

        multiSelectionState.observe(context as LifecycleOwner, Observer {
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

        selectedClips.observe(context as LifecycleOwner, Observer {
            when {
                it.contains(clip) -> {
                    holder.itemView.mainCard.setCardBackgroundColor(CARD_SELECTED_COLOR)
                }
                clip.toDisplay -> {
                    holder.itemView.mainCard.setCardBackgroundColor(CARD_COLOR)
                }
                else -> {
                    /**
                     * We are also checking if selected item is this clip. Since for large item set
                     * it kinda forgets about it due to recreation of whole list.
                     */
                    if (selectedItem.value != clip)
                        holder.itemView.mainCard.setCardBackgroundColor(Color.TRANSPARENT)
                }
            }
        })
    }

    private fun setPinMovements(
        clip: Clip,
        holder: MainHolder
    ) {
        /** TODO: Fix this, if possible */
        /*holder.itemView.ci_pinImage.setImageDrawable(
            VectorDrawableCompat.create(context.resources, R.drawable.ic_pin, context.theme)
        )*/
        if (clip.isPinned) {
            setButtonDrawable(
                holder.itemView,
                R.drawable.ic_unpin
            )
            holder.itemView.ci_btn_pin.text = context.getString(R.string.unpin)
            holder.itemView.ci_pinImage.show()
        } else {
            setButtonDrawable(
                holder.itemView,
                R.drawable.ic_pin
            )
            holder.itemView.ci_btn_pin.text = context.getString(R.string.pin)
            holder.itemView.ci_pinImage.collapse()
        }
    }

    private fun setButtonDrawable(view: View, @DrawableRes imageId: Int) {
        view.ci_btn_pin.setCompoundDrawablesWithIntrinsicBounds(
            null,
            ContextCompat.getDrawable(context, imageId),
            null, null
        )
    }

    private fun setTags(view: View, clip: Clip) {
        view.ci_tagLayout.removeAllViews()
        clip.tags?.forEach mainLoop@{ entry ->
            if (entry.key.isNotBlank()) {
                val textView = LayoutInflater.from(context)
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

    /* fun submitList(list: ArrayList<Clip>) {
         this.list = list
         notifyDataSetChanged()
     }*/

    enum class MENU_TYPE {
        Edit, Pin, Special, Share
    }

    class MainHolder(view: View) : RecyclerView.ViewHolder(view)
}

