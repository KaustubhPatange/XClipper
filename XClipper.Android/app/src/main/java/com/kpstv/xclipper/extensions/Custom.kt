package com.kpstv.xclipper.extensions

import com.ferfalk.simplesearchview.SimpleSearchView
import com.kpstv.xclipper.App.STANDARD_DATE_FORMAT
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


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

fun ioThread(block: suspend (() -> Unit)) {
    Coroutines.io { block.invoke() }
}

fun mainThread(block: suspend (() -> Unit)) {
    Coroutines.main { block.invoke() }
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

fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>> {
    return lazy {
        GlobalScope.async(start = CoroutineStart.LAZY) {
            block.invoke(this)
        }
    }
}

fun Date.getFormattedDate(): String =
    SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.US).format(this)


/**
 * Basically checks if string is an enum of a particular class.
 * If yes then returns the enum else null.
 *
 * Source: https://stackoverflow.com/a/41855007/10133501
 */
inline fun <reified T : Enum<T>> enumValueOrNull(name: String): T? {
    return enumValues<T>().find { it.name == name }
}


