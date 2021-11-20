package com.kpstv.xclipper.extensions

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.firebase.database.DataSnapshot
import com.google.gson.annotations.SerializedName
import com.kpstv.xclipper.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

typealias FirebaseFunction = (DataSnapshot) -> Unit

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

fun logger(TAG: String, message: String) {
    if (BuildConfig.DEBUG)
        Log.e(TAG, message)
}

fun logger(TAG: String, message: String, exception: Exception) {
    if (BuildConfig.DEBUG)
        Log.e(TAG, message, exception)
}

fun Boolean.toInt(): Int = if (this) 1 else 0

private const val STANDARD_DATE_FORMAT = "yyyyMMddHHmmss"
fun Date.getFormattedDate(): String =
    SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.US).format(this)

fun String.toLines(): List<String> {
    return this.split("[\n|\r]".toRegex())
}

fun <T> DiffUtil.ItemCallback<T>.asConfig(): AsyncDifferConfig<T> {
    return AsyncDifferConfig.Builder(this)
        .setBackgroundThreadExecutor(Dispatchers.Default.asExecutor())
        .build()
}

infix fun Boolean.xnor(other: Boolean): Boolean = (this xor other).not()
