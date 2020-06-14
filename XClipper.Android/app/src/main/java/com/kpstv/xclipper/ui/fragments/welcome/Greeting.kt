package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isAndroid10orUp
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment

class Greeting : Fragment(R.layout.fragment_welcome) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Palette 1

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette1,
            nextPaletteId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) R.color.palette_android else R.color.palette2,
            textId = R.string.palette1_text,
            nextTextId = if (isAndroid10orUp()) R.string.next_1 else R.string.nextd_1,
            action = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    findNavController().navigate(GreetingDirections.actionFragmentGreetToAndroid10())
                else
                    findNavController().navigate(GreetingDirections.actionFragmentGreetToTurnOnService())
            }
        )
    }
}