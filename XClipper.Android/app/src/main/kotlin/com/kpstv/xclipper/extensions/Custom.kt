package com.kpstv.xclipper.extensions

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.annotations.SerializedName
import com.kpstv.firebase.DataResponse
import com.kpstv.xclipper.App.STANDARD_DATE_FORMAT
import com.kpstv.xclipper.BuildConfig
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

typealias SimpleFunction = () -> Unit
typealias ErrorFunction = (Exception?) -> Unit
typealias FirebaseFunction = (DataSnapshot) -> Unit
typealias FragClazz = KClass<out Fragment>

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

fun SimpleSearchView.setOnSearchCloseListener(block: SimpleFunction) {
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

fun logger(TAG: String, message: String) {
    if (BuildConfig.DEBUG)
        Log.e(TAG, message)
}

fun logger(TAG: String, message: String, exception: Exception) {
    if (BuildConfig.DEBUG)
        Log.e(TAG, message, exception)
}

fun Boolean.toInt(): Int = if (this) 1 else 0

fun Date.getFormattedDate(): String =
    SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.US).format(this)

enum class LicenseType {
    @SerializedName("0")
    Standard,
    @SerializedName("1")
    Premium,
    @SerializedName("2")
    Invalid
}

/**
 * Basically checks if string is an enum of a particular class.
 * If yes then returns the enum else null.
 *
 * Source: https://stackoverflow.com/a/41855007/10133501
 */
inline fun <reified T : Enum<T>> enumValueOrNull(name: String): T? {
    return enumValues<T>().find { it.name == name }
}

fun String.toLines(): List<String> {
    return this.split("[\n|\r]".toRegex())
}

/**
 * If there is two same classes like one maybe domain & other may be entity,
 * following method might help to map such instance to other classes.
 *
 * Source: https://gist.github.com/KaustubhPatange/089792e18c19783247bb5b554e4ccaee
 */
inline fun <reified F : Any, reified T : Any> mapToClass(from: F, convertType: (String, Any?) -> Any? = { _,v -> v }): T {
    val args = HashMap<KParameter, Any?>()
    val params: List<KParameter> = T::class.constructors.first().parameters
    F::class.memberProperties.forEach { prop: KProperty1<out F, Any?> ->
        if (prop.visibility == KVisibility.PUBLIC) {
            val kParam: KParameter = params.first { it.name == prop.name }
            val value: Any? = prop.getter.call(from)
            args[kParam] = convertType.invoke(kParam.name!!, value)
        }
    }
    return T::class.constructors.first().callBy(args)
}

fun <T> DiffUtil.ItemCallback<T>.asConfig(): AsyncDifferConfig<T> {
    return AsyncDifferConfig.Builder(this)
        .setBackgroundThreadExecutor(Dispatchers.Default.asExecutor())
        .build()
}
