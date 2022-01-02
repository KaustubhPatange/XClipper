package com.kpstv.onboarding.welcome

import com.kpstv.onboarding.di.navigation.OnBoardingNavigation
import com.kpstv.welcome.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class WindowApp : AbstractWelcomeFragment() {

    @Inject
    lateinit var onBoardingNavigation: OnBoardingNavigation

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette6,
        nextPaletteId = R.color.palette7,
        textId = R.string.palette6_text,
        nextTextId = R.string.next_8,
        isLastScreen = true, // last screen must add @AndroidEntryPoint annotation
        action = {
            onBoardingNavigation.goToNext()
        }
    )
}