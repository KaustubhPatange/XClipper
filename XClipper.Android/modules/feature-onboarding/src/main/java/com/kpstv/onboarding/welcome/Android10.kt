package com.kpstv.onboarding.welcome

import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.welcome.R

internal class Android10 : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette_android,
        nextPaletteId = R.color.palette2,
        textId = R.string.palette_android_text,
        nextTextId = R.string.next_2,
        directions = OnBoardingRoutes.TURN_ON_SERVICE
    )
}