package com.kpstv.xclipper.ui.fragments.welcome

import android.view.LayoutInflater
import androidx.navigation.fragment.findNavController
import com.kpstv.xclipper.R
import kotlinx.android.synthetic.main.item_gifview.view.*

class QuickSettingTitle : AbstractWelcomeFragment() {

    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette5,
        nextPaletteId = R.color.palette6,
        textId = R.string.palette5_text,
        nextTextId = R.string.next_6,
        directions = QuickSettingTitleDirections.actionQuickSettingTitleToWindowApp(),
        insertView = LayoutInflater.from(requireContext()).inflate(
            R.layout.item_gifview, null
        ).apply {
            gifImageView.setImageResource(R.drawable.feature_quicksetting)
        }
    )
}