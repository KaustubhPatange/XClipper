package com.kpstv.xclipper.extensions

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.Px
import androidx.core.view.children
import kotlin.reflect.KClass

fun View.globalVisibleRect(): Rect {
    val rect = Rect()
    getGlobalVisibleRect(rect)
    return rect
}

fun View.setPadding(@Px horizontally: Int, @Px vertically: Int) {
    setPadding(horizontally, vertically, horizontally, vertically)
}

fun View.runBlinkEffect(color: Int = Color.WHITE, times: Int = 3) {
    val drawable = background
    val fromColor = if (drawable is ColorDrawable) drawable.color else Color.TRANSPARENT
    ValueAnimator.ofArgb(fromColor, color, fromColor).apply {
        addUpdateListener {
            setBackgroundColor(it.animatedValue as Int)
        }
        repeatCount = (times - 1).coerceAtLeast(0)
        repeatMode = ValueAnimator.REVERSE
        start()
    }
}

/* Find extensions */

fun View.findViewByText(text: String, ignoreCase: Boolean = true) : TextView? {
    if (this is TextView && this.text.contentEquals(text, ignoreCase)) return this
    if (this is ViewGroup) {
        children.forEach { view ->
            view.findViewByText(text, ignoreCase)?.let { return it }
        }
    }
    return null
}

inline fun <reified T: ViewGroup> View.findParent(identifier: Int = -1) : T? = findParent(T::class, identifier)
fun <T: ViewGroup> View.findParent(clazz: KClass<T>, identifier: Int = -1) : T? {
    val parent = parent
    if (parent != null && parent is View) {
        if (parent::class.qualifiedName == clazz.qualifiedName) {
            if (identifier != -1 && parent.id == identifier) return parent as? T
            return parent as? T
        } else {
            return parent.findParent(clazz, identifier)
        }
    }
    return null
}