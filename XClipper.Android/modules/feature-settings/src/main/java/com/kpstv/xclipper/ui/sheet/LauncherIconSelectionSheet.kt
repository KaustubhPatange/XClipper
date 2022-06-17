package com.kpstv.xclipper.ui.sheet

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_settings.R
import com.kpstv.xclipper.ui.helpers.AppThemeHelper
import dev.sasikanth.colorsheet.ColorSheet
import dev.sasikanth.colorsheet.databinding.ColorItemBinding
import dev.sasikanth.colorsheet.databinding.ColorSheetBinding

class LauncherIconSelectionSheet : ColorSheet() {
    fun interface Callback {
        fun onIconSelected(index: Int)
    }
    private val binding by viewBinding(ColorSheetBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sheetTitle.text = getString(R.string.li_sheet_title)
        binding.colorSheetClose.setOnClickListener { dismiss() }

        binding.colorSheetList.adapter = Adapter(
            mipmaps = AppThemeHelper.baseIcons,
            selectedIndex = AppThemeHelper.baseIcons.indexOf(AppThemeHelper.launcherIconMipmapRes()),
            onIconSelected = { (parentFragment as? Callback)?.onIconSelected(it) }
        )
    }

    companion object {
        fun show(fragmentManager: FragmentManager) {
            val sheet = LauncherIconSelectionSheet()
            sheet.show(fragmentManager, null)
        }
    }

    private class Adapter(
        private val mipmaps: List<Int>,
        private val selectedIndex: Int,
        private val onIconSelected: (index: Int) -> Unit
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder.create(parent, onIconSelected)
        override fun getItemCount(): Int = mipmaps.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(mipmaps[position], selectedIndex == position)
        }

        class ViewHolder(private val binding: ColorItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(mipmap: Int, isSelected: Boolean) {
                binding.colorSelectedCircle.setImageResource(mipmap)
                binding.colorSelectedBorder.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
                binding.colorSelected.hide()
            }
            companion object {
                fun create(parent: ViewGroup, onIconSelected: (index: Int) -> Unit) : ViewHolder {
                    val binding = ColorItemBinding.inflate(parent.context.layoutInflater(), parent, false)
                    val holder = ViewHolder(binding)
                    binding.root.setOnClickListener { onIconSelected(holder.adapterPosition) }
                    return holder
                }
            }
        }
    }
}
