package com.kpstv.xclipper.ui.fragments.welcome

import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isClipboardAccessibilityServiceRunning
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showAccessibilityDialog
import com.kpstv.xclipper.ui.activities.Start

class TurnOnService : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette2,
        nextPaletteId = R.color.palette3,
        textId = R.string.palette2_text,
        nextTextId = if (Utils.isAndroid10orUp()) R.string.next_3 else R.string.nextd_2,
        action = {
            if (!isClipboardAccessibilityServiceRunning(requireContext())) {
                showAccessibilityDialog(requireContext())
            } else
                navigateTo(Start.Screen.ENABLE_SUGGESTIONS)
        }
    )

    override fun onResume() {
        super.onResume()
        if (isClipboardAccessibilityServiceRunning(requireContext()))
            navigateTo(Start.Screen.ENABLE_SUGGESTIONS)
    }
}