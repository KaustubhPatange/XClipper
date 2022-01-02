package com.kpstv.onboarding.welcome

import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.welcome.R
import com.kpstv.welcome.databinding.ItemGifviewBinding

internal class QuickSettingTitle : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration {
        val gifImageView = ItemGifviewBinding.inflate(layoutInflater, null, false)
        gifImageView.root.setImageResource(R.drawable.feature_quicksetting)

        return Configuration(
            paletteId = R.color.palette5,
            nextPaletteId = R.color.palette6,
            textId = R.string.palette5_text,
            nextTextId = R.string.next_7,
            directions = OnBoardingRoutes.WINDOWS_APP,
            insertView = gifImageView.root
        )
    }
}