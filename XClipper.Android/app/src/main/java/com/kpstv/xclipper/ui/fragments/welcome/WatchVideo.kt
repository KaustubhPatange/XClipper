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

@AndroidEntryPoint
class WatchVideo : Fragment(R.layout.fragment_welcome) {

    @Inject lateinit var preferenceProvider: PreferenceProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette7,
            nextPaletteId = R.color.palette8,
            textId = R.string.palette7_text,
            nextTextId = R.string.next_8,
            action = {
                preferenceProvider.putBooleanKey(TUTORIAL_PREF, true)

                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_greet, true)
                    .build()

                findNavController().navigate(
                    WatchVideoDirections.actionWatchVideoToFragmentHome(),
                    options
                )
            }
        )
    }
}