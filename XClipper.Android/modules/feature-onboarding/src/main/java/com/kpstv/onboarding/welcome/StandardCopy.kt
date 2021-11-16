package com.kpstv.onboarding.welcome

import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.welcome.R
import com.kpstv.welcome.databinding.ItemGifviewBinding

internal class StandardCopy : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration {
        val gifImageView = ItemGifviewBinding.inflate(layoutInflater, null, false)
        gifImageView.root.setImageResource(R.drawable.feature_xcopy)

        return Configuration(
            paletteId = R.color.palette4,
            nextPaletteId = R.color.palette5,
            textId = R.string.palette4_text,
            nextTextId = R.string.next_6,
            directions = OnBoardingRoutes.QUICK_SETTING_TITLE,
            insertView = gifImageView.root
        )
    }
}