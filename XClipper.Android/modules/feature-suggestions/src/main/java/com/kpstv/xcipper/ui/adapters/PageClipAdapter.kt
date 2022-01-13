package com.kpstv.xcipper.ui.adapters

import android.content.ClipData
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xcipper.di.navigation.SpecialActionsLauncher
import com.kpstv.xcipper.feature_suggestions.R
import com.kpstv.xcipper.feature_suggestions.databinding.ItemBubbleServiceBinding
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.extensions.asConfig
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.extensions.show
import es.dmoral.toasty.Toasty

class PageClipAdapter(
    private val clipboardProvider: ClipboardProvider,
    private val specialActionsLauncher: SpecialActionsLauncher,
    val onClick: (String) -> Unit
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
                    ContextCompat.getColor(
                        ibcTextView.context,
                        R.color.colorSelectedClip
                    )
                )
            else
                ibcTextView.setTextColor(
                    ContextCompat.getColor(
                        ibcTextView.context,
                        android.R.color.white
                    )
                )

            ibcTextView.text = clip?.data
            ibcTextView.setOnClickListener {
                onClick.invoke(clip?.data!!)
            }
            ibcTextView.setOnLongClickListener {
                specialActionsLauncher.launch(clip?.data!!)
                true
            }
            btnCopy.setOnClickListener {
                clipboardProvider.setClipboard(clip?.data!!)
                Toasty.info(root.context, root.context.getString(R.string.copy_to_clipboard)).show()
            }
        }
    }
}