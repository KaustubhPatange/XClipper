package com.kpstv.xclipper.ui.fragments.welcome

import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.ItemGifviewBinding
import com.kpstv.xclipper.ui.activities.Start

class QuickSettingTitle : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration {
        val gifImageView = ItemGifviewBinding.inflate(layoutInflater, null, false)
        gifImageView.root.setImageResource(R.drawable.feature_quicksetting)

        return Configuration(
            paletteId = R.color.palette5,
            nextPaletteId = R.color.palette6,
            textId = R.string.palette5_text,
            nextTextId = R.string.next_7,
            directions = Start.Screen.WINDOWS_APP,
            insertView = gifImageView.root
        )
    }
}