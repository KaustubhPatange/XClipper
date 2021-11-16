package com.kpstv.onboarding.utils

import android.os.Build

object OnBoardUtils {
    fun isAndroid10orUp() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}