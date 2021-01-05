package com.kpstv.xclipper.ui.fragments.welcome

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kpstv.xclipper.R
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.WelcomeUtils.Companion.setUpFragment
import kotlinx.android.synthetic.main.item_gifview.view.*

class EnableSuggestion : Fragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragment(
            view = view,
            activity = requireActivity(),
            paletteId = R.color.palette3,
            nextPaletteId = R.color.palette4,
            textId = R.string.palette3_text,
            nextTextId = if (Utils.isAndroid10orUp()) R.string.next_4 else R.string.nextd_3,
            action = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    findNavController().navigate(EnableSuggestionDirections.actionEnableSuggestionToStandardCopy())
                else
                    findNavController().navigate(EnableSuggestionDirections.actionEnableSuggestionToWindowApp())
            },
            insertView = LayoutInflater.from(requireContext()).inflate(
                R.layout.item_gifview, null
            ).apply {
                gifImageView.setImageResource(R.drawable.feature_suggestion)
            }
        )
    }
}
