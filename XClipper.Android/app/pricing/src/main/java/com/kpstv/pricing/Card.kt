package com.kpstv.pricing

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.kpstv.pricing.databinding.PriceItemBinding
import com.kpstv.pricing.databinding.PriceItemBinding.*
import com.kpstv.xclipper.extensions.layoutInflater

class Card @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: PriceItemBinding = inflate(context.layoutInflater(), this, true)

    init {
        initStyle(attrs, defStyleAttr)
    }

    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) = with(binding) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.Card, defStyleAttr, 0)

        if (typedArray.hasValue(R.styleable.Card_purchaseType))
            purchaseType.text = typedArray.getString(R.styleable.Card_purchaseType)

        if (typedArray.hasValue(R.styleable.Card_purchaseTypeColor))
            purchaseType.setTextColor(
                typedArray.getColor(
                    R.styleable.Card_purchaseTypeColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_separatorColor))
            separator1.setBackgroundColor(
                typedArray.getColor(
                    R.styleable.Card_separatorColor,
                    ContextCompat.getColor(context, R.color.black)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_purchaseAmount))
            purchaseAmount.text = typedArray.getString(R.styleable.Card_purchaseAmount)

        if (typedArray.hasValue(R.styleable.Card_purchaseAmountColor))
            purchaseAmount.setTextColor(
                typedArray.getColor(
                    R.styleable.Card_purchaseAmountColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_purchaseDescription))
            purchaseDescription.text = typedArray.getString(R.styleable.Card_purchaseDescription)

        if (typedArray.hasValue(R.styleable.Card_purchaseDescriptionColor))
            purchaseDescription.setTextColor(
                typedArray.getColor(
                    R.styleable.Card_purchaseDescriptionColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_purchaseButtonText))
            purchaseButton.text = typedArray.getString(R.styleable.Card_purchaseButtonText)

        if (typedArray.hasValue(R.styleable.Card_purchaseButtonTextColor))
            purchaseButton.setTextColor(
                typedArray.getColor(
                    R.styleable.Card_purchaseButtonTextColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_purchaseButtonDrawable))
            purchaseButton.setIcon(
                typedArray.getDrawable(
                    R.styleable.Card_purchaseButtonDrawable
                )
            )

        if (typedArray.hasValue(R.styleable.Card_purchaseButtonColor))
            purchaseButton.backgroundTintList = ColorStateList.valueOf(
                typedArray.getColor(
                    R.styleable.Card_purchaseButtonColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_cardBackgroundColor))
            mainCard.setCardBackgroundColor(
                typedArray.getColor(
                    R.styleable.Card_cardBackgroundColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_cardElevation)) {
            mainCard.cardElevation = typedArray.getDimension(R.styleable.Card_cardElevation, 0f)
        }

        typedArray.recycle()
    }

    fun setPurchaseAmount(amount: String) {
        binding.purchaseAmount.text = amount
    }

    fun setButtonClickListener(block: OnClickListener?) = binding.purchaseButton.setOnClickListener(block)

    fun setButtonClickListener(block: (View?) -> Unit) = binding.purchaseButton.setOnClickListener(block)

    fun setButtonText(text: String) {
        binding.purchaseButton.text = text
    }

    fun setButtonColor(@ColorInt color: Int) {
        binding.purchaseButton.backgroundTintList = ColorStateList.valueOf(color)
    }

    fun setButtonIcon(icon: Drawable?) {
        binding.purchaseButton.icon = icon
    }
}
