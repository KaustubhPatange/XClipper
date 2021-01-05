package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WindowApp: Fragment(R.layout.fragment_welcome) {

    @Inject lateinit var preferenceProvider: PreferenceProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette6,
            nextPaletteId = R.color.palette7,
            textId = R.string.palette6_text,
            nextTextId = R.string.next_8, /*if (Utils.isAndroid10orUp()) R.string.next_7 else R.string.nextd_4*/
            isLastScreen = true,
            action = {
                preferenceProvider.putBooleanKey(App.TUTORIAL_PREF, true)

                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_greet, true)
                    .build()

                findNavController().navigate(
                    WindowAppDirections.actionWatchVideoToFragmentHome(),
                    options
                )
            }
        )
    }
}