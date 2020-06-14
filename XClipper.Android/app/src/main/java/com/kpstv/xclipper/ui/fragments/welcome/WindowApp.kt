package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment

class WindowApp: Fragment(R.layout.fragment_welcome) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette6,
            nextPaletteId = R.color.palette7,
            textId = R.string.palette6_text,
            nextTextId = if (Utils.isAndroid10orUp()) R.string.next_7 else R.string.nextd_4,
            action = WindowAppDirections.actionWindowAppToWatchVideo()
        )
    }
}