package com.kpstv.xclipper.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import kotlinx.android.synthetic.main.content_item.view.*


class CIAdapter(
    private val context: Context,
    private val multiSelectionState: LiveData<Boolean>,
    private val selectedItem: LiveData<Clip>,
    var list: ArrayList<Clip> = ArrayList(),
    private val onClick: (Clip, Int) -> Unit,
    private val onLongClick: (Clip, Int) -> Unit,
    private val selectedClips: LiveData<ArrayList<Clip>>
) :
    RecyclerView.Adapter<CIAdapter.MainHolder>() {

    private val TAG = javaClass.simpleName

    private lateinit var copyClick: (Clip, Int) -> Unit
    private lateinit var menuClick: (Clip, Int, MENU_TYPE) -> Unit
    private val CARD_COLOR = ContextCompat.getColor(context,R.color.colorCard)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder =
        MainHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.content_item, parent, false)
        )

    override fun onBindViewHolder(holder: MainHolder, position: Int) {

        val clip = list[position]

        holder.itemView.ci_textView.text = clip.data?.Decrypt()

        holder.itemView.mainCard.setOnClickListener { onClick.invoke(clip, position) }

        holder.itemView.ci_timeText.text = clip.timeString

      /*  if (clip.toDisplay) {
            holder.itemView.mainCard.setCardBackgroundColor(CARD_COLOR)
            holder.itemView.hiddenLayout.visibility = View.VISIBLE
        } else {
            holder.itemView.mainCard.setCardBackgroundColor(Color.TRANSPARENT)
            holder.itemView.hiddenLayout.visibility = View.GONE
        }*/

        holder.itemView.mainCard.setOnLongClickListener {
            onLongClick.invoke(clip, position)
            true
        }
        holder.itemView.ci_copyButton.setOnClickListener { copyClick.invoke(clip, position) }
        holder.itemView.ci_btn_edit.setOnClickListener { menuClick.invoke(clip, position, MENU_TYPE.Edit) }
        holder.itemView.ci_btn_delete.setOnClickListener { menuClick.invoke(clip, position, MENU_TYPE.Delete) }
        holder.itemView.ci_btn_share.setOnClickListener { menuClick.invoke(clip, position, MENU_TYPE.Share) }

        selectedItem.observe(context as LifecycleOwner, Observer {
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
            } else {
                holder.itemView.ci_copyButton.show()
                holder.itemView.ci_timeText.show()
            }
        })

        selectedClips.observe(context as LifecycleOwner, Observer {
            when {
                it.contains(clip) -> {
                    holder.itemView.mainCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSelected))
                }
                clip.toDisplay -> {
                    holder.itemView.mainCard.setCardBackgroundColor(CARD_COLOR)
                }
                else -> {
                    holder.itemView.mainCard.setCardBackgroundColor(Color.TRANSPARENT)
                }
            }
        })
    }

    fun setCopyClick(block : (Clip, Int) -> Unit) {
        this.copyClick = block;
    }

    fun setMenuItemClick(block: (Clip, Int, MENU_TYPE) -> Unit) {
        this.menuClick = block;
    }

    fun submitList(list: ArrayList<Clip>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int = list.size

    enum class MENU_TYPE {
        Edit, Delete, Share
    }

    class MainHolder(view: View) : RecyclerView.ViewHolder(view)
}