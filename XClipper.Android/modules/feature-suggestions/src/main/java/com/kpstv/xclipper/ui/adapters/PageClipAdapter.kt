package com.kpstv.xclipper.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.feature_suggestions.R
import com.kpstv.xclipper.feature_suggestions.databinding.ItemBubbleServiceBinding
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.extensions.asConfig
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.extensions.show

class PageClipAdapter(
    private val clipboardProvider: ClipboardProvider,
    val onClick: (String) -> Unit,
    val onLongClick: (String) -> Unit,
    val onCopyClick: (String) -> Unit,
) :
    PagedListAdapter<Clip, PageClipAdapter.PageClipHolder>(DiffUtils.asConfig()) {

    companion object {
        private val DiffUtils = object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Clip, newItem: Clip) =
                oldItem == newItem
        }
    }

    class PageClipHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PageClipHolder(
            ItemBubbleServiceBinding.inflate(
                parent.context.layoutInflater(),
                parent,
                false
            ).root
        )

    override fun onBindViewHolder(holder: PageClipHolder, position: Int) {
        val clip = getItem(position)
        with(ItemBubbleServiceBinding.bind(holder.itemView)) {

            /** This will show a small line which indicates this is a pinned clip. */
            if (clip?.isPinned == true)
                ibcPinView.show()
            else
                ibcPinView.hide()

            if (clipboardProvider.getCurrentClip().value == clip?.data)
                ibcTextView.setTextColor(
                    ibcTextView.context.getColorAttr(R.attr.colorCurrentClip)
                )
            else
                ibcTextView.setTextColor(
                    ibcTextView.context.getColorAttr(R.attr.colorTextPrimary)
                )

            ibcTextView.text = clip?.data
            ibcTextView.setOnClickListener {
                onClick.invoke(clip?.data!!)
            }
            ibcTextView.setOnLongClickListener {
                onLongClick.invoke(clip?.data!!)
                true
            }
            btnCopy.setOnClickListener {
                onCopyClick.invoke(clip?.data!!)
            }
        }
    }
}