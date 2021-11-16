package com.kpstv.onboarding.internals

import com.kpstv.onboarding.welcome.Android10
import com.kpstv.onboarding.welcome.EnableSuggestion
import com.kpstv.onboarding.welcome.Greeting
import com.kpstv.onboarding.welcome.ImproveDetection
import com.kpstv.onboarding.welcome.QuickSettingTitle
import com.kpstv.onboarding.welcome.StandardCopy
import com.kpstv.onboarding.welcome.TurnOnService
import com.kpstv.onboarding.welcome.WindowApp
import com.kpstv.xclipper.extensions.FragClazz

internal enum class OnBoardingRoutes(val clazz: FragClazz) {
    GREET(Greeting::class),
    ANDROID10(Android10::class),
    TURN_ON_SERVICE(TurnOnService::class),
    IMPROVE_DETECTION(ImproveDetection::class),
    ENABLE_SUGGESTIONS(EnableSuggestion::class),
    STANDARD_COPY(StandardCopy::class),
    QUICK_SETTING_TITLE(QuickSettingTitle::class),
    WINDOWS_APP(WindowApp::class),
}