package com.kpstv.xclipper.extensions

import android.view.View
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipEntry
import kotlinx.coroutines.*


class FValueEventListener(
    val onDataChange: (DataSnapshot) -> Unit,
    val onError: (DatabaseError) -> Unit
) :
    ValueEventListener {
    override fun onDataChange(data: DataSnapshot) = onDataChange.invoke(data)
    override fun onCancelled(error: DatabaseError) = onError.invoke(error)
}

enum class Status {
    Success,
    Error
}

/*class OnSearchViewListener(
    val onSubmit: ((String) -> Unit)?,
    val onChange: ((String) -> Unit)?
) : SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            onSubmit.invoke(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            onChange.invoke(newText)
        }
        return true
    }
}*/

fun SimpleSearchView.setOnQueryTextListener(
    onSubmit: ((String) -> Unit)? = null,
    onChange: ((String) -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            if (query != null) onSubmit?.invoke(query)
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText != null) onChange?.invoke(newText)
            return true
        }

        override fun onQueryTextCleared(): Boolean {
            onClear?.invoke()
            return true
        }
    })
}

fun SimpleSearchView.setOnSearchCloseListener(block: () -> Unit) {
    setOnSearchViewListener(object : SimpleSearchView.SearchViewListener {
        override fun onSearchViewShownAnimation() {

        }

        override fun onSearchViewClosed() {
            block.invoke()
        }

        override fun onSearchViewClosedAnimation() {
        }

        override fun onSearchViewShown() {

        }

    })
}

fun List<Clip>.cloneToEntries(): List<ClipEntry> {
    val list = ArrayList<ClipEntry>()
    this.forEach {
        list.add(ClipEntry.from(it))
    }
    return list
}

fun List<Clip>.cloneForAdapter(): List<Clip> {
    this.forEach {
        Clip.autoFill(it)
    }
    return this
}

fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>> {
    return lazy {
        GlobalScope.async(start = CoroutineStart.LAZY) {
            block.invoke(this)
        }
    }
}

fun Clip.clone(data: String): Clip {
    val clip = Clip(id, data, time)
    clip.timeString = timeString
    clip.toDisplay = toDisplay
    return clip
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.collpase() {
    this.visibility = View.GONE
}
