package com.kpstv.xclipper.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.converters.DateFormatConverter
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipEntry
import kotlinx.android.synthetic.main.content_item.view.*

class CIAdapter(
    private val context: Context,
    var list: List<Clip> = ArrayList(),
    val onClick: (Clip, Int) -> Unit
) :
    RecyclerView.Adapter<CIAdapter.MainHolder>() {

    private lateinit var copyClick: (Clip, Int) -> Unit
    private lateinit var menuClick: (Clip, Int, MENU_TYPE) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder =
        MainHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.content_item, parent, false)
        )

    override fun onBindViewHolder(holder: MainHolder, position: Int) {

        val clip = list[position]

        holder.itemView.ci_textView.text = clip.data?.Decrypt()

        holder.itemView.mainCard.setOnClickListener { onClick.invoke(clip, position) }

        holder.itemView.ci_timeText.text = clip.timeString

        if (clip.toDisplay) {
            holder.itemView.mainCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorCard
                )
            )
            holder.itemView.hiddenLayout.visibility = View.VISIBLE
        } else {
            holder.itemView.mainCard.setCardBackgroundColor(Color.TRANSPARENT)
            holder.itemView.hiddenLayout.visibility = View.GONE
        }

        holder.itemView.ci_copyButton.setOnClickListener { copyClick.invoke(clip, position) }
        holder.itemView.ci_btn_edit.setOnClickListener { menuClick.invoke(clip, position, MENU_TYPE.Edit) }
        holder.itemView.ci_btn_delete.setOnClickListener { menuClick.invoke(clip, position, MENU_TYPE.Delete) }
        holder.itemView.ci_btn_share.setOnClickListener { menuClick.invoke(clip, position, MENU_TYPE.Share) }

    }

    fun setCopyClick(block : (Clip, Int) -> Unit) {
        this.copyClick = block;
    }

    fun setMenuItemClick(block: (Clip, Int, MENU_TYPE) -> Unit) {
        this.menuClick = block;
    }

    fun submitList(list: List<Clip>) {
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