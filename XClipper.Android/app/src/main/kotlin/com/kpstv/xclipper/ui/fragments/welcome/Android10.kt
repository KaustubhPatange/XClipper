package com.kpstv.xclipper.ui.fragments.welcome

import com.kpstv.xclipper.R

class Android10 : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette_android,
        nextPaletteId = R.color.palette2,
        textId = R.string.palette_android_text,
        nextTextId = R.string.next_2,
        directions = Android10Directions.actionAndroid10ToTurnOnService()
    )
}