package com.kpstv.xclipper.ui.fragments.settings

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.doOnNextLayout
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.findParent
import com.kpstv.xclipper.extensions.findViewByText
import com.kpstv.xclipper.extensions.runBlinkEffect
import com.kpstv.xclipper.extensions.utils.Utils

abstract class AbstractPreferenceFragment : PreferenceFragmentCompat() {

    /**
     * Highlight preference item
     */
    fun highlightItemWithTitle(title: String) {
        val view = view ?: throw IllegalStateException("Cannot highlight item when the view is null")
        val color = Utils.getDataFromAttr(requireContext(), R.attr.colorTextSecondary)
        view.doOnNextLayout {
            val itemView = (view as ViewGroup).findViewByText(title)?.findParent<LinearLayout>()
            itemView?.findParent<RecyclerView>()?.let { recyclerView ->
                val position = recyclerView.layoutManager?.getPosition(itemView) ?: 0
                recyclerView.scrollToPosition(position)
            }
            itemView?.runBlinkEffect(color = color)
        }
    }
}