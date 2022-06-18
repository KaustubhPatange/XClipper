package com.kpstv.xclipper.ui.fragments.custom

import android.annotation.SuppressLint
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
import androidx.preference.PreferenceGroupAdapter
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

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("RestrictedApi")
    fun <T : ViewGroup> observeOnCustomPreferenceInvalidate(key: String, block: T.() -> Unit) {
        val view = view ?: throw IllegalStateException("Cannot perform this action before onViewCreated()")

        view.doOnLayout {
            val recyclerView = recyclerView ?: throw IllegalStateException("RecyclerView not found")
            val adapter = adapter ?: throw IllegalStateException("Could not find adapter")
            val layoutManager = recyclerView.layoutManager ?: throw IllegalStateException("Could not find LayoutManager")

            fun findAdapterView() : View? {
                val position = adapter.getPreferenceAdapterPosition(key)
                return if (position != -1) {
                    layoutManager.findViewByPosition(position)
                } else null
            }

            val preference = findPreference<Preference>(key)
            val position = adapter.getPreferenceAdapterPosition(key)
            if (preference != null && position == -1) {
                // special case: preference might be hidden.
            }

            val customAdapterView = findAdapterView()
            if (customAdapterView != null) { block(customAdapterView as T) }
            recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    val childView = findAdapterView()
                    if (childView === view) block(it as T)
                }
                override fun onChildViewDetachedFromWindow(view: View) {}
            })
        }
    }

    fun observeOnPreferenceInvalidate(preference: Preference, block: Preference.() -> Unit) {
        val view = view ?: throw IllegalStateException("Cannot perform this action before onViewCreated()")

        view.doOnLayout {
            val recyclerView = recyclerView ?: throw IllegalStateException("RecyclerView not found")
            val first = recyclerView.children.firstOrNull { it.findViewById<TextView>(android.R.id.title)?.text == preference.title }
            if (first != null) {
                block(preference)
            }
            recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                var isViewRemoved = false
                override fun onChildViewAttachedToWindow(child: View) {
                    if (isViewRemoved && child.findViewById<TextView>(android.R.id.title)?.text == preference.title) {
                        block(preference)
                    }
                }
                override fun onChildViewDetachedFromWindow(child: View) {
                    if (child.findViewById<TextView>(android.R.id.title)?.text == preference.title) {
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

    val Preference.view : LinearLayout? get() {
        return titleView?.findParent()
    }

    val Preference.imageView: ImageView? get() {
        return view?.findViewById(android.R.id.icon)
    }

    val recyclerView: RecyclerView? get() {
        return requireView().findViewById(R.id.recycler_view)
    }

    val adapter: PreferenceGroupAdapter? get() {
        return recyclerView?.adapter as? PreferenceGroupAdapter
    }

}