package com.kpstv.onboarding.welcome

import android.os.Build
import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.onboarding.utils.OnBoardUtils.isAndroid10orUp
import com.kpstv.welcome.R

internal class Greeting : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette1,
        nextPaletteId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) R.color.palette_android else R.color.palette2,
        textId = R.string.palette1_text,
        nextTextId = if (isAndroid10orUp()) R.string.next_1 else R.string.nextd_1,
        directions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            OnBoardingRoutes.ANDROID10
        else
            OnBoardingRoutes.TURN_ON_SERVICE
    )
}