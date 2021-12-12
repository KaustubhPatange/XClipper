package com.kpstv.xclipper.extensions.elements

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.kpstv.core.R
import com.kpstv.core.databinding.CustomPricingCardBinding
import com.kpstv.xclipper.extensions.layoutInflater

class PricingCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: CustomPricingCardBinding = CustomPricingCardBinding.inflate(context.layoutInflater(), this, true)

    init {
        initStyle(attrs, defStyleAttr)
    }

    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) = with(binding) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.PricingCardView, defStyleAttr, 0)

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseType))
            purchaseType.text = typedArray.getString(R.styleable.PricingCardView_purchaseType)

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseTypeColor))
            purchaseType.setTextColor(
                typedArray.getColor(
                    R.styleable.PricingCardView_purchaseTypeColor,
                    ContextCompat.getColor(context, android.R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.PricingCardView_separatorColor))
            separator1.setBackgroundColor(
                typedArray.getColor(
                    R.styleable.PricingCardView_separatorColor,
                    ContextCompat.getColor(context, android.R.color.black)
                )
            )

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseAmount))
            purchaseAmount.text = typedArray.getString(R.styleable.PricingCardView_purchaseAmount)

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseAmountColor))
            purchaseAmount.setTextColor(
                typedArray.getColor(
                    R.styleable.PricingCardView_purchaseAmountColor,
                    ContextCompat.getColor(context, android.R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseDescription))
            purchaseDescription.text = typedArray.getString(R.styleable.PricingCardView_purchaseDescription)

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseDescriptionColor))
            purchaseDescription.setTextColor(
                typedArray.getColor(
                    R.styleable.PricingCardView_purchaseDescriptionColor,
                    ContextCompat.getColor(context, android.R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseButtonText))
            purchaseButton.text = typedArray.getString(R.styleable.PricingCardView_purchaseButtonText)

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseButtonTextColor))
            purchaseButton.setTextColor(
                typedArray.getColor(
                    R.styleable.PricingCardView_purchaseButtonTextColor,
                    ContextCompat.getColor(context, android.R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseButtonDrawable))
            purchaseButton.setIcon(
                typedArray.getDrawable(
                    R.styleable.PricingCardView_purchaseButtonDrawable
                )
            )

        if (typedArray.hasValue(R.styleable.PricingCardView_purchaseButtonColor))
            purchaseButton.backgroundTintList = ColorStateList.valueOf(
                typedArray.getColor(
                    R.styleable.PricingCardView_purchaseButtonColor,
                    ContextCompat.getColor(context, android.R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.PricingCardView_cardBackgroundColor))
            mainCard.setCardBackgroundColor(
                typedArray.getColor(
                    R.styleable.PricingCardView_cardBackgroundColor,
                    ContextCompat.getColor(context, android.R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.PricingCardView_cardElevation)) {
            mainCard.cardElevation = typedArray.getDimension(R.styleable.PricingCardView_cardElevation, 0f)
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
