package com.kpstv.xclipper.ui.fragments.welcome

import com.kpstv.navigation.AnimationDefinition
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.ui.activities.Start
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WindowApp : AbstractWelcomeFragment() {

    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette6,
        nextPaletteId = R.color.palette7,
        textId = R.string.palette6_text,
        nextTextId = R.string.next_8,
        isLastScreen = true,
        action = {
            preferenceProvider.putBooleanKey(App.TUTORIAL_PREF, true)

            navigateTo(Start.Screen.HOME, true, AnimationDefinition.CircularReveal())
        }
    )
}