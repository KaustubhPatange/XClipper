package com.kpstv.xclipper.ui.fragments.welcome

import android.view.LayoutInflater
import com.kpstv.xclipper.R
import com.kpstv.xclipper.ui.activities.Start
import kotlinx.android.synthetic.main.item_gifview.view.*

class StandardCopy : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette4,
        nextPaletteId = R.color.palette5,
        textId = R.string.palette4_text,
        nextTextId = R.string.next_5,
        directions = Start.Screen.QUICK_SETTING_TITLE,
        insertView = LayoutInflater.from(context).inflate(R.layout.item_gifview, null).apply {
            gifImageView.setImageResource(R.drawable.feature_xcopy)
        }
    )
}