package com.kpstv.xclipper.ui.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.core.R
import com.kpstv.core.databinding.DialogMultiSelectBinding
import com.kpstv.core.databinding.DialogMultiSelectItemBinding
import com.kpstv.xclipper.extensions.collapse
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.extensions.show

data class MultiSelectModel3(val title: String, val subtitle: String?, val isChecked: Boolean) {
    internal companion object {
        internal fun from(data: MultiSelectModel2): MultiSelectModel3 = MultiSelectModel3(
            title = data.title,
            subtitle = null,
            isChecked = data.isChecked
        )
    }
}

data class MultiSelectModel2(val title: String, val isChecked: Boolean)

class MultiSelectDialogBuilder(context: Context, private val itemsCheckedState: (Map<Int, Boolean>) -> Unit) : AlertDialog.Builder(context) {
    private val binding = DialogMultiSelectBinding.inflate(context.layoutInflater())
    private var adapter: MultiSelectAdapter3? = null

    private fun getPositiveListener(listener: DialogInterface.OnClickListener?): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { dialog, which ->
            itemsCheckedState(adapter?.getItemsCheckState() ?: mapOf())
            listener?.onClick(dialog, which)
        }
    }
    init {
        setView(binding.root)
        setPositiveButton(context.getString(R.string.alright), null)
    }
    @JvmName("setItems2")
    fun setItems(items: List<MultiSelectModel2>) {
        adapter = MultiSelectAdapter2(items)
    }
    @JvmName("setItems3")
    fun setItems(items: List<MultiSelectModel3>) {
        adapter = MultiSelectAdapter3(items)
    }

    override fun setTitle(titleId: Int): AlertDialog.Builder {
        binding.spacerTop.show()
        return super.setTitle(titleId)
    }
    override fun setTitle(title: CharSequence?): AlertDialog.Builder {
        binding.spacerTop.show()
        return super.setTitle(title)
    }
    override fun setPositiveButton(textId: Int, listener: DialogInterface.OnClickListener?): AlertDialog.Builder {
        return super.setPositiveButton(textId, getPositiveListener(listener))
    }
    override fun setPositiveButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): AlertDialog.Builder {
        return super.setPositiveButton(text, getPositiveListener(listener))
    }
    override fun create(): AlertDialog {
        binding.recyclerView.adapter = adapter
        return super.create()
    }
}

private class MultiSelectAdapter2(items: List<MultiSelectModel2>) : MultiSelectAdapter3(items.map { MultiSelectModel3.from(it) })
private open class MultiSelectAdapter3(items: List<MultiSelectModel3>) : RecyclerView.Adapter<MultiSelectHolder>() {
    private val internalMutableList = items.toMutableList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiSelectHolder = MultiSelectHolder.create(parent)
    override fun onBindViewHolder(holder: MultiSelectHolder, position: Int) {
        val (title, subtitle, checked) = internalMutableList[position]
        holder.bind(title, subtitle, checked) { value ->
            val item = internalMutableList[position]
            internalMutableList[position] = item.copy(isChecked = value)
            notifyItemChanged(position)
        }
    }
    fun getItemsCheckState(): Map<Int, Boolean> {
        return internalMutableList.associate { internalMutableList.indexOf(it) to it.isChecked }
    }
    override fun getItemCount(): Int = internalMutableList.size
}

private class MultiSelectHolder(private val binding: DialogMultiSelectItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(title: String, subtitle: String?, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        binding.tvTitle.text = title
        if (subtitle == null) {
            binding.tvSubtitle.collapse()
        } else {
            binding.tvSubtitle.text = subtitle
        }
        binding.checkbox.isChecked = isChecked
        binding.checkbox.isClickable = false
        binding.root.setOnClickListener {
            onCheckedChange(!isChecked)
        }
    }

    companion object {
        fun create(parent: ViewGroup): MultiSelectHolder {
            return MultiSelectHolder(DialogMultiSelectItemBinding.inflate(parent.context.layoutInflater(), parent, false))
        }
    }
}
