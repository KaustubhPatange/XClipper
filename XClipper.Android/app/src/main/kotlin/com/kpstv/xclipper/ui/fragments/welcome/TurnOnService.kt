package com.kpstv.xclipper.ui.fragments.welcome

import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.ClipListConverter
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showAccessibilityDialog
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.service.helper.ClipboardLogDetector
import com.kpstv.xclipper.ui.activities.Start

class TurnOnService : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette2,
        nextPaletteId = conditionalNavigationPalette(),
        textId = R.string.palette2_text,
        nextTextId = if (Utils.isAndroid10orUp()) R.string.next_3 else R.string.nextd_2,
        action = {
            if (!ClipboardAccessibilityService.isRunning(requireContext())) {
                showAccessibilityDialog(requireContext())
            } else conditionalNavigation()
        }
    )

    override fun onResume() {
        super.onResume()
        if (ClipboardAccessibilityService.isRunning(requireContext())) conditionalNavigation()
    }

    private fun conditionalNavigation() {
        if (ClipboardLogDetector.isDetectionCompatible(requireContext())) {
            navigateTo(Start.Screen.ENABLE_SUGGESTIONS)
        } else {
            navigateTo(Start.Screen.IMPROVE_DETECTION)
        }
    }

    private fun conditionalNavigationPalette() : Int {
        return if (ClipboardLogDetector.isDetectionCompatible(requireContext())) {
            R.color.palette3
        } else {
            R.color.palette_improve
        }
    }
}