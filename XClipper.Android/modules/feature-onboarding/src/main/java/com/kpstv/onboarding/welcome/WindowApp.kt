package com.kpstv.onboarding.welcome

import com.kpstv.welcome.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class WindowApp : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette6,
        nextPaletteId = R.color.palette7,
        textId = R.string.palette6_text,
        nextTextId = R.string.next_8,
        isLastScreen = true, // last screen must add @AndroidEntryPoint annotation
        action = {
            // TODO: Add here the hilt magic
            //navigateTo(OnBoardingRoutes.HOME, true, AnimationDefinition.CircularReveal())
        }
    )
}