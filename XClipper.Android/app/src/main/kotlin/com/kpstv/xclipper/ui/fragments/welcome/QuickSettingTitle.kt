package com.kpstv.xclipper.ui.fragments.welcome

import android.view.LayoutInflater
import com.kpstv.xclipper.R
import com.kpstv.xclipper.ui.activities.Start
import kotlinx.android.synthetic.main.item_gifview.view.*

class QuickSettingTitle : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette5,
        nextPaletteId = R.color.palette6,
        textId = R.string.palette5_text,
        nextTextId = R.string.next_7,
        directions = Start.Screen.WINDOWS_APP,
        insertView = LayoutInflater.from(requireContext()).inflate(
            R.layout.item_gifview, null
        ).apply {
            gifImageView.setImageResource(R.drawable.feature_quicksetting)
        }
    )
}