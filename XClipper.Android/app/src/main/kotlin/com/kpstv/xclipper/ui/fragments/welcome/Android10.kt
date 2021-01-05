package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment

class Android10 : Fragment(R.layout.fragment_welcome) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette_android,
            nextPaletteId = R.color.palette2,
            textId = R.string.palette_android_text,
            nextTextId = R.string.next_2,
            action = Android10Directions.actionAndroid10ToTurnOnService()
        )

        // Linkify.addLinks(fw_textView, Linkify.WEB_URLS)
    }
}