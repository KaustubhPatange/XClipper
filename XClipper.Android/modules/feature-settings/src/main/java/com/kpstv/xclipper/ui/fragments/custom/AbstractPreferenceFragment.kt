package com.kpstv.xclipper.ui.fragments.custom

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.xclipper.extensions.findParent
import com.kpstv.xclipper.extensions.findViewByText
import com.kpstv.xclipper.extensions.getColorAttr
import com.kpstv.xclipper.extensions.runBlinkEffect
import com.kpstv.xclipper.feature_settings.R

abstract class AbstractPreferenceFragment : PreferenceFragmentCompat() {

    /**
     * Highlight preference item
     */
    fun highlightItemWithTitle(title: String) {
        val view = view ?: throw IllegalStateException("Cannot highlight item when the view is null")
        val color = requireContext().getColorAttr(R.attr.colorTextSecondary)
        view.doOnNextLayout {
            val itemView = (view as ViewGroup).findViewByText(title)?.findParent<LinearLayout>()
            itemView?.findParent<RecyclerView>()?.let { recyclerView ->
                val position = recyclerView.layoutManager?.getPosition(itemView) ?: 0
                recyclerView.scrollToPosition(position)
            }
            itemView?.runBlinkEffect(color = color)
        }
    }

    fun observeOnPreferenceInvalidate(preference: Preference, block: () -> Unit) {
        val view = view ?: throw IllegalStateException("Cannot perform this action before onViewCreated()")

        view.doOnLayout {
            val recyclerView = (view as ViewGroup).findViewById<RecyclerView>(R.id.recycler_view)
            val first = recyclerView.children.firstOrNull { it.findViewById<TextView>(android.R.id.title).text == preference.title }
            if (first != null) {
                block()
            }
            recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                var isViewRemoved = false
                override fun onChildViewAttachedToWindow(child: View) {
                    if (isViewRemoved && child.findViewById<TextView>(android.R.id.title).text == preference.title) {
                        block()
                    }
                }
                override fun onChildViewDetachedFromWindow(child: View) {
                    if (child.findViewById<TextView>(android.R.id.title).text == preference.title) {
                        isViewRemoved = true
                    }
                }
            })
        }
    }

    val Preference.titleView : TextView? get() {
        val view = requireView()
        return view.findViewByText(title.toString())
    }

    val Preference.view : View? get() {
        return titleView?.findParent<LinearLayout>()
    }

    val Preference.imageView: ImageView? get() {
        return view?.findViewById(android.R.id.icon)
    }

}