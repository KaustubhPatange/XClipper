package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.kpstv.xclipper.App.TUTORIAL_PREF
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Was initially made to let user know that there is get started video for
 * the app. But the welcome screens are more than enough to let them
 * understand on the app works.
 */
@Deprecated("Should be removed") // TODO:
@AndroidEntryPoint
class WatchVideo : AbstractWelcomeFragment() {

    @Inject lateinit var preferenceProvider: PreferenceProvider

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette7,
        nextPaletteId = R.color.palette8,
        textId = R.string.palette7_text,
        nextTextId = R.string.next_8,
        action = {
            preferenceProvider.putBooleanKey(TUTORIAL_PREF, true)

            val options = NavOptions.Builder()
                .setPopUpTo(R.id.fragment_greet, true)
                .build()

            navigateTo(WatchVideoDirections.actionWatchVideoToFragmentHome(), options)
        }
    )
}