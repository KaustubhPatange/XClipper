package com.kpstv.pricing

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.price_item.view.*


class Card @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context!!, attrs, defStyleAttr) {

    init {
        inflate()
        initStyle(attrs, defStyleAttr)
    }


    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.price_item, this, true)
    }

    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.Card, defStyleAttr, 0)

        if (typedArray.hasValue(R.styleable.Card_purchaseType))
            purchase_type.text = typedArray.getString(R.styleable.Card_purchaseType)

        if (typedArray.hasValue(R.styleable.Card_purchaseTypeColor))
            purchase_type.setTextColor(
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
            purchase_amount.text = typedArray.getString(R.styleable.Card_purchaseAmount)

        if (typedArray.hasValue(R.styleable.Card_purchaseAmountColor))
            purchase_amount.setTextColor(
                typedArray.getColor(
                    R.styleable.Card_purchaseAmountColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_purchaseDescription))
            purchase_description.text = typedArray.getString(R.styleable.Card_purchaseDescription)

        if (typedArray.hasValue(R.styleable.Card_purchaseDescriptionColor))
            purchase_description.setTextColor(
                typedArray.getColor(
                    R.styleable.Card_purchaseDescriptionColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_purchaseButtonText))
            purchase_button.text = typedArray.getString(R.styleable.Card_purchaseButtonText)

        if (typedArray.hasValue(R.styleable.Card_purchaseButtonTextColor))
            purchase_button.setTextColor(
                typedArray.getColor(
                    R.styleable.Card_purchaseButtonTextColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            )

        if (typedArray.hasValue(R.styleable.Card_purchaseButtonColor))
            purchase_button.backgroundTintList = ColorStateList.valueOf(
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

        typedArray.recycle()
    }

    fun setPurchaseAmount(amount: String) {
        purchase_amount.text = amount
    }

    fun setButtonClickListener(block: OnClickListener?) = purchase_button.setOnClickListener(block)

    fun setButtonClickListener(block: (View?) -> Unit) = purchase_button.setOnClickListener(block)
}
