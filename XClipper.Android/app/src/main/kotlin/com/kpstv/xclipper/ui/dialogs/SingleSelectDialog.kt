package com.kpstv.xclipper.ui.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.DialogMultiSelectBinding
import com.kpstv.xclipper.databinding.DialogMultiSelectItemBinding
import com.kpstv.xclipper.extensions.*

data class SingleSelectModel(val title: String)
data class SingleSelectModel2(val title: String, val subtitle: String?) {
    constructor(title: String) : this(title, null)
}

class SingleSelectDialogBuilder(context: Context, onSelect: (position: Int) -> Unit) : AlertDialog.Builder(context) {
    private val binding = DialogMultiSelectBinding.inflate(context.layoutInflater())
    private var adapter: SingleSelectAdapter2? = null

    private var dialog: AlertDialog? = null

    private val highlightItemPositions = arrayListOf<Int>()

    private val onSelectListener : (Int) -> Unit = { position : Int ->
        onSelect(position)
        dialog?.dismiss()
    }

    init {
        setView(binding.root)
        super.setPositiveButton(context.getString(R.string.cancel), null)
    }

    @JvmName("setItems2")
    fun setItems(items: List<SingleSelectModel2>) {
        adapter = SingleSelectAdapter2(items, onSelectListener)
    }
    @JvmName("setItems3")
    fun setItems(items: List<SingleSelectModel>) {
        adapter = SingleSelectAdapter(items, onSelectListener)
    }

    fun highLightItemPosition(position: Int) {
        highlightItemPositions.add(position)
    }

    override fun setTitle(titleId: Int): AlertDialog.Builder {
        binding.spacerTop.show()
        return super.setTitle(titleId)
    }
    override fun setTitle(title: CharSequence?): AlertDialog.Builder {
        binding.spacerTop.show()
        return super.setTitle(title)
    }

    override fun create(): AlertDialog {
        adapter?.setHighlightedItemPosition(highlightItemPositions)
        binding.recyclerView.adapter = adapter
        dialog = super.create()
        return dialog!!
    }

    override fun setPositiveButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): AlertDialog.Builder = this
    override fun setPositiveButton(textId: Int, listener: DialogInterface.OnClickListener?): AlertDialog.Builder = this
}

private class SingleSelectAdapter(items: List<SingleSelectModel>, private val onSelect: (position: Int) -> Unit)
    : SingleSelectAdapter2(items.map { SingleSelectModel2(it.title) }, onSelect)
private open class SingleSelectAdapter2(private val items: List<SingleSelectModel2>, private val onSelect: (position: Int) -> Unit) : RecyclerView.Adapter<SingleSelectHolder>() {
    private val highlightItemPositions = arrayListOf<Int>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleSelectHolder {
        return SingleSelectHolder(
            DialogMultiSelectItemBinding.inflate(parent.context.layoutInflater(), parent, false).apply {
                checkbox.collapse()
            }
        )
    }

    override fun onBindViewHolder(holder: SingleSelectHolder, position: Int) {
        val item = items[position]
        holder.bind(
            item = item,
            highlight = highlightItemPositions.contains(position),
            onSelect = { onSelect(position) }
        )
    }

    override fun getItemCount(): Int = items.size

    fun setHighlightedItemPosition(position: List<Int>) {
        highlightItemPositions.addAll(position)
    }
}

private class SingleSelectHolder(private val binding: DialogMultiSelectItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: SingleSelectModel2, highlight: Boolean, onSelect: () -> Unit) {
        binding.tvTitle.text = item.title
        if (item.subtitle != null) {
            binding.tvSubtitle.show()
            binding.tvSubtitle.text = item.subtitle
        } else {
            binding.tvSubtitle.collapse()
        }
        val highlightColor = binding.tvTitle.context.getColorAttr(R.attr.colorCurrentClip)
        val subtitleColor = binding.tvTitle.context.getColorAttr(R.attr.colorTextSecondary)
        if (highlight) {
            binding.tvTitle.setTextColor(highlightColor)
            binding.tvSubtitle.setTextColor(ColorUtils.blendARGB(subtitleColor, highlightColor, 0.5f))
        } else {
            binding.tvTitle.setTextColor(binding.tvTitle.context.getColorAttr(R.attr.colorTextPrimary))
            binding.tvSubtitle.setTextColor(subtitleColor)
        }
        binding.root.setOnClickListener { onSelect() }
    }
}