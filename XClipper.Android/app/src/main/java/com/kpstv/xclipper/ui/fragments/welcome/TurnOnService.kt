package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isClipboardAccessibilityServiceRunning
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showAccessibilityDialog
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment

class TurnOnService : Fragment(R.layout.fragment_welcome) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette2,
            nextPaletteId = R.color.palette3,
            text = SpannableString(getString(R.string.palette2_text)),
            nextTextId = R.string.next_2_5,
            action = {
                if (!isClipboardAccessibilityServiceRunning(requireContext())) {
                    showAccessibilityDialog(requireContext())
                } else
                    findNavController().navigate(TurnOnServiceDirections.actionTurnOnServiceToWatchVideo())
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (isClipboardAccessibilityServiceRunning(requireContext()))
            findNavController().navigate(TurnOnServiceDirections.actionTurnOnServiceToWatchVideo())
    }
}