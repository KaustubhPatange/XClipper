package com.kpstv.xclipper.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.core_addons.R
import com.kpstv.xclipper.core_addons.databinding.ItemExtensionBinding
import com.kpstv.xclipper.data.model.ExtensionAdapterItem
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.extensions.setDefaultCardColor
import com.kpstv.xclipper.ui.helpers.ExtensionHelper
import java.util.*

class ExtensionAdapter(
    private val items: List<ExtensionAdapterItem>,
    private val viewLifecycleOwner: LifecycleOwner,
    private val provideExtensionHelper: (ExtensionAdapterItem) -> ExtensionHelper,
    private val itemClickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<ExtensionAdapter.ExtensionViewHolder>() {
    private val extensionHelpersMap = WeakHashMap<String, ExtensionHelper>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtensionViewHolder =
        ExtensionViewHolder(
            ItemExtensionBinding.inflate(parent.context.layoutInflater(), parent, false)
        )

    override fun onBindViewHolder(holder: ExtensionViewHolder, position: Int) {
        val item = items[position]
        val helper = extensionHelpersMap[item.sku] ?: run {
            val helper = provideExtensionHelper.invoke(item)
            extensionHelpersMap[item.sku] = helper
            helper
        }

        holder.itemView.tag = item.sku

        holder.bind(item)

        helper.observePurchaseComplete().asLiveData().observe(viewLifecycleOwner) { unlocked ->
            if (unlocked) {
                holder.bindAdditionalForUnlock(item)
            } else {
                holder.bindAdditionalForNotUnlocked()
            }
        }

        holder.setClickListener { itemClickListener.invoke(position) }
    }

    override fun onViewDetachedFromWindow(holder: ExtensionViewHolder) {
        extensionHelpersMap.remove(holder.itemView.tag)
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int = items.size

    inner class ExtensionViewHolder(private val binding: ItemExtensionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExtensionAdapterItem) {
            binding.tvTitle.text = item.title
            binding.ivLogo.setImageResource(item.icon)
            binding.tvDesc.text = item.smallDescription
        }

        fun setClickListener(listener: View.OnClickListener) {
            binding.root.setOnClickListener(listener)
        }

        fun bindAdditionalForUnlock(item: ExtensionAdapterItem) {
            binding.ivLogo.imageTintList = ColorStateList.valueOf(item.dominantColor)
            binding.cardLogo.setCardBackgroundColor(ColorUtils.blendARGB(item.dominantColor, Color.BLACK, 0.4f))
        }

        fun bindAdditionalForNotUnlocked() {
            binding.ivLogo.imageTintList = ColorStateList.valueOf(
                binding.root.context.getColorAttr(R.attr.colorTextSecondary)
            )
            binding.cardLogo.setDefaultCardColor()
        }
    }
}