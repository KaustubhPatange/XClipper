package com.kpstv.onboarding.welcome

import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.onboarding.utils.OnBoardUtils.isAndroid10orUp
import com.kpstv.welcome.R
import com.kpstv.xclipper.extensions.helper.ClipboardLogDetector
import com.kpstv.xclipper.service.ClipboardAccessibilityService
import com.kpstv.xclipper.ui.dialogs.ClipboardServiceDialogs

internal class TurnOnService : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette2,
        nextPaletteId = conditionalNavigationPalette(),
        textId = R.string.palette2_text,
        nextTextId = if (isAndroid10orUp()) R.string.next_3 else R.string.nextd_2,
        action = {
            if (!ClipboardAccessibilityService.isRunning(requireContext())) {
                ClipboardServiceDialogs.showAccessibilityDialog(requireContext())
            } else conditionalNavigation()
        }
    )

    override fun onResume() {
        super.onResume()
        if (ClipboardAccessibilityService.isRunning(requireContext())) conditionalNavigation()
    }

    private fun conditionalNavigation() {
        if (ClipboardLogDetector.isDetectionCompatible(requireContext())) {
            navigateTo(OnBoardingRoutes.ENABLE_SUGGESTIONS)
        } else {
            navigateTo(OnBoardingRoutes.IMPROVE_DETECTION)
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