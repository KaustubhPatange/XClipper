package com.kpstv.xclipper.extensions

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Px
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import com.kpstv.xclipper.extensions.ColorExUtils.isDarkColor
import kotlin.reflect.KClass

fun View.globalVisibleRect(): Rect {
    val rect = Rect()
    getGlobalVisibleRect(rect)
    return rect
}

fun View.setPadding(@Px horizontally: Int, @Px vertically: Int) {
    setPadding(horizontally, vertically, horizontally, vertically)
}

/**
 * Run blink effect. Setting [color] to -1 will try to auto detect the
 * color from the view background or maybe theme.
 */
fun View.runBlinkEffect(color: Int = -1, times: Int = 3) {
    val drawable = if (this !is CardView) background else ColorDrawable(cardBackgroundColor.defaultColor)
    val fromColor = if (drawable is ColorDrawable) drawable.color else Color.TRANSPARENT

    var finalColor = if (color == -1) Color.WHITE else color
    if (fromColor != 0 && color == -1) {
        finalColor = if (isDarkColor(fromColor)) {
            ColorUtils.blendARGB(fromColor, Color.WHITE, 0.5f)
        } else {
            ColorUtils.blendARGB(fromColor, Color.BLACK, 0.5f)
        }
    }

    ValueAnimator.ofArgb(fromColor, finalColor, fromColor).apply {
        addUpdateListener {
            setBackgroundColor(it.animatedValue as Int)
            if (this@runBlinkEffect is CardView) setCardBackgroundColor(it.animatedValue as Int)
        }
        addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                if (this@runBlinkEffect is CardView) setCardBackgroundColor((drawable as ColorDrawable).color)
                else background = drawable
            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
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
@Suppress("UNCHECKED_CAST")
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