package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Build
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isAndroid10orUp

class Greeting : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette1,
        nextPaletteId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) R.color.palette_android else R.color.palette2,
        textId = R.string.palette1_text,
        nextTextId = if (isAndroid10orUp()) R.string.next_1 else R.string.nextd_1,
        directions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            GreetingDirections.actionFragmentGreetToAndroid10()
        else
            GreetingDirections.actionFragmentGreetToTurnOnService()
    )
}