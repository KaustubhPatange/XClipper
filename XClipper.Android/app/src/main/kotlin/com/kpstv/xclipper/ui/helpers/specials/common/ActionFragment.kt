package com.kpstv.xclipper.ui.helpers.specials.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.navigation.ValueFragment
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.getAttrResourceId
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.ui.helpers.specials.SpecialAction
import com.kpstv.xclipper.ui.helpers.specials.SpecialSettings

abstract class ActionFragment : ValueFragment() {

    private val specialSettings by lazy { SpecialSettings(requireContext()) }
    private val items = arrayListOf<ActionItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    fun setCommonItem(title: String, message: String, icon: Int) {
        items.add(CommonItem(title = title, message = message, icon = icon))
    }

    fun setCheckedItem(title: String, message: String, icon: Int, action: SpecialAction) {
        items.add(CheckableItem(title = title, message = message, icon = icon, action = action, isChecked = specialSettings.getCheckSetting(action)))
    }

    abstract fun getItemClickListener(item: ActionItem, position: Int)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerview = view as RecyclerView
        recyclerview.adapter = ActionAdapter(
            specialSettings = specialSettings,
            items = items,
            itemClickListener = ::getItemClickListener
        )
    }

    private class ActionAdapter(
        private val specialSettings: SpecialSettings,
        private val items: List<ActionItem>,
        private val itemClickListener: (ActionItem, position: Int) -> Unit
    ) : RecyclerView.Adapter<ActionAdapter.ActionHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionHolder {
            val layoutInflater = parent.context.layoutInflater()
            val view = layoutInflater.inflate(androidx.preference.R.layout.preference_information_material, parent, false)
            if (viewType == CheckableItem.viewType) {
                // checkable item
                layoutInflater.inflate(
                    androidx.preference.R.layout.preference_widget_switch_compat,
                    view.findViewById(android.R.id.widget_frame),
                    true
                )
            }
            return ActionHolder(view)
        }

        override fun onBindViewHolder(holder: ActionHolder, position: Int) {
            val item = items[position]

            holder.bind(
                item = item
            ) { itemClickListener(item, position) }
        }

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int): Int {
            return if (items[position] is CheckableItem) CheckableItem.viewType else CommonItem.viewType
        }

        private inner class ActionHolder(private val view: View) : RecyclerView.ViewHolder(view) {

            private val icon = view.findViewById<ImageView>(android.R.id.icon)
            private val title = view.findViewById<TextView>(android.R.id.title)
            private val message = view.findViewById<TextView>(android.R.id.summary)

            private val switch by lazy { view.findViewById<SwitchCompat>(androidx.preference.R.id.switchWidget) }

            init {
                val resourceId = view.context.getAttrResourceId(android.R.attr.selectableItemBackground)
                view.setBackgroundResource(resourceId)
            }

            fun bind(item: ActionItem, onClickListener: SimpleFunction) {
                title.text = item.title
                message.text = item.message
                icon.setImageResource(item.icon)

                if (item is CheckableItem) {
                    switch.isChecked = item.isChecked
                }

                view.setOnClickListener {
                    if (item is CheckableItem) {
                        val setting = !item.isChecked
                        item.isChecked = setting
                        specialSettings.checkSetting(item.action, setting)
                        switch.isChecked = setting
                    }
                    onClickListener()
                }
            }
        }
    }
}

interface ActionItem {
    val title: String
    val message: String
    @get:DrawableRes
    val icon: Int

    interface ViewType {
        val viewType: Int
    }
}

data class CommonItem(
    override val title: String, override val message: String, override val icon: Int
) : ActionItem {
    companion object : ActionItem.ViewType {
        override val viewType: Int = 0
    }
}

data class CheckableItem(
    override val title: String,
    override val message: String,
    override val icon: Int,
    val action: SpecialAction,
    var isChecked: Boolean,
) : ActionItem {
    companion object : ActionItem.ViewType {
        override val viewType: Int = 1
    }
}