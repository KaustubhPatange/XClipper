package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.fragment.app.Fragment
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment

class Greeting : Fragment(R.layout.fragment_welcome) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Palette 1

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette1,
            nextPaletteId = R.color.palette2,
            text = SpannableString(getString(R.string.palette1_text)),
            nextTextId = R.string.next_1_5,
            action = GreetingDirections.actionFragmentGreetToTurnOnService()
        )

    }
}