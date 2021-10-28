package com.kpstv.xclipper.ui.fragments.welcome

import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.ItemGifviewBinding
import com.kpstv.xclipper.ui.activities.Start

class StandardCopy : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration {
        val gifImageView = ItemGifviewBinding.inflate(layoutInflater, null, false)
        gifImageView.root.setImageResource(R.drawable.feature_xcopy)

        return Configuration(
            paletteId = R.color.palette4,
            nextPaletteId = R.color.palette5,
            textId = R.string.palette4_text,
            nextTextId = R.string.next_6,
            directions = Start.Screen.QUICK_SETTING_TITLE,
            insertView = gifImageView.root
        )
    }
}