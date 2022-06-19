package com.kpstv.xclipper.extensions

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.*
import androidx.core.graphics.ColorUtils
import androidx.core.view.updatePadding
import com.kpstv.core.R
import com.kpstv.core.databinding.CustomQuickTipBinding

private const val QUICK_TIP_TAG = "quick_tip_layout"

class QuickTipContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    init {
        // animateLayoutChanges = true
        layoutTransition = LayoutTransition()
    }
    override fun getOrientation(): Int = VERTICAL
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (child.tag != QUICK_TIP_TAG) throw IllegalArgumentException("Only QuickTips can be the direct child of the QuickTipContainer.")
        super.addView(child, index, params)
    }
}

class QuickTip(private val quickTipView: FrameLayout) {

    constructor(containerView: ViewGroup) : this(CustomQuickTipBinding.inflate(containerView.context.layoutInflater()).root)

    private val context = quickTipView.context
    private val binding = CustomQuickTipBinding.bind(quickTipView)

    init {
        binding.root.tag = QUICK_TIP_TAG
        setIcon(null)
        setPositiveButton(context.getString(R.string.ok), null)
    }

    fun setTitleText(@StringRes resId: Int) {
        binding.tvTitle.show()
        binding.tvTitle.setText(resId)
    }

    fun setSubText(@StringRes resId: Int) {
        binding.tvSubtitle.setText(resId)
    }

    fun setIcon(@DrawableRes resId: Int) {
        setIcon(context.drawableFrom(resId))
    }

    fun setIcon(drawable: Drawable?) {
        if (drawable != null) {
            binding.icon.setImageDrawable(drawable)
            binding.icon.show()
        } else {
            binding.icon.hide()
        }
    }

    fun setTipColor(@ColorInt color: Int) {
        binding.card.setCardBackgroundColor(color)
    }

    fun setTipColorRes(@ColorRes id: Int) {
        binding.card.setCardBackgroundColor(context.colorFrom(id))
    }

    fun setIconTint(@ColorInt color: Int) {
        binding.icon.imageTintList = ColorStateList.valueOf(color)
    }

    // Apply color as tint to icon & set background color according to it
    fun applyColor(@ColorInt color: Int) {
        val backgroundColor = context.getColorAttr(R.attr.background)
        if (ColorExUtils.isDarkColor(backgroundColor)) {
            setIconTint(color)
            setTipColor(ColorUtils.blendARGB(color, Color.BLACK, 0.6f)) // dark
        } else {
            setIconTint(ColorUtils.blendARGB(color, Color.BLACK, 0.3f))
            setTipColor(ColorUtils.blendARGB(color, Color.WHITE, 0.2f)) // light
        }
    }

    fun setPositiveButton(@StringRes buttonTextId: Int, listener: SimpleFunction?) {
        setPositiveButton(context.getString(buttonTextId), listener)
    }

    fun setPositiveButton(buttonText: String, listener: SimpleFunction?) {
        binding.btnPositive.text = buttonText
        binding.btnPositive.setOnClickListener {
            listener?.invoke()
        }
    }

    fun setNegativeButton(@StringRes buttonTextId: Int, listener: SimpleFunction?) {
        setNegativeButton(context.getString(buttonTextId), listener)
    }

    fun setNegativeButton(buttonText: String, listener: SimpleFunction?) {
        binding.btnNegative.show()
        binding.btnNegative.text = buttonText
        binding.btnNegative.setOnClickListener {
            listener?.invoke()
        }
    }

    fun hideButtonPanel() {
        binding.buttonPannel.collapse()
    }

    fun setOnClick(listener: SimpleFunction) {
        binding.card.isClickable = true
        binding.card.isFocusable = true
        binding.card.setOnClickListener { listener.invoke() }
    }

    fun setOnLongClick(listener: SimpleFunction) {
        binding.card.isClickable = true
        binding.card.isFocusable = true
        binding.card.setOnLongClickListener {
            listener.invoke()
            true
        }
    }

    fun updatePadding(@Px left: Int = 0, @Px top: Int = 0, @Px right: Int = 0, @Px bottom: Int = 0) {
        binding.root.updatePadding(
            binding.root.paddingLeft + left,
            binding.root.paddingTop + top,
            binding.root.paddingRight + right,
            binding.root.paddingBottom + bottom
        )
    }

    fun dismiss() {
        quickTipView.removeView(binding.root)
    }

    fun create(): View = binding.root

    fun Int.dp() : Int = (this * context.resources.displayMetrics.density).toInt()
}