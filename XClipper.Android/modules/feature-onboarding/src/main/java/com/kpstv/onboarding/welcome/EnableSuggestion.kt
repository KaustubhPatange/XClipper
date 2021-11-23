package com.kpstv.onboarding.welcome

import android.annotation.SuppressLint
import android.os.Build
import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.onboarding.utils.OnBoardUtils
import com.kpstv.welcome.R
import com.kpstv.welcome.databinding.ItemGifviewBinding
import com.kpstv.xclipper.extensions.utils.SystemUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class EnableSuggestion : AbstractWelcomeFragment() {

    @SuppressLint("NewApi")
    override fun getConfigurations(): Configuration {
        val gifImageView = ItemGifviewBinding.inflate(layoutInflater, null, false)
        gifImageView.root.setImageResource(R.drawable.feature_suggestion)

        return Configuration(
            paletteId = R.color.palette3,
            nextPaletteId = R.color.palette4,
            textId = R.string.palette3_text,
            nextTextId = if (OnBoardUtils.isAndroid10orUp()) R.string.next_5 else R.string.nextd_3,
            action = {
                if (!SystemUtils.isSystemOverlayEnabled(requireContext())) {
                    //TODO:showOverlayDialog(requireContext())
                } else {
                    navigateToNextScreen()
                }
            },
            insertView = gifImageView.root
        )
    }

    override fun onResume() {
        super.onResume()
        appSettings.setShowClipboardSuggestions(SystemUtils.isSystemOverlayEnabled(requireContext()))
    }

    private fun navigateToNextScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            navigateTo(OnBoardingRoutes.STANDARD_COPY)
        else
            navigateTo(OnBoardingRoutes.WINDOWS_APP)
    }
}
