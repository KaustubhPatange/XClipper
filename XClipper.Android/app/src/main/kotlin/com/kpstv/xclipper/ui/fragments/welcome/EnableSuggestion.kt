package com.kpstv.xclipper.ui.fragments.welcome

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isSystemOverlayEnabled
import com.kpstv.xclipper.extensions.utils.Utils.Companion.showOverlayDialog
import com.kpstv.xclipper.ui.activities.Start
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.item_gifview.view.*
import javax.inject.Inject

@AndroidEntryPoint
class EnableSuggestion : AbstractWelcomeFragment() {

    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    @SuppressLint("NewApi")
    override fun getConfigurations(): Configuration = Configuration(
        paletteId = R.color.palette3,
        nextPaletteId = R.color.palette4,
        textId = R.string.palette3_text,
        nextTextId = if (Utils.isAndroid10orUp()) R.string.next_4 else R.string.nextd_3,
        action = {
            if (!isSystemOverlayEnabled(requireContext())) {
                showOverlayDialog(requireContext())
            } else {
                navigateToNextScreen()
            }
        },
        insertView = LayoutInflater.from(requireContext()).inflate(R.layout.item_gifview, null)
            .apply {
                gifImageView.setImageResource(R.drawable.feature_suggestion)
            }
    )

    override fun onResume() {
        super.onResume()
        App.showSuggestion = isSystemOverlayEnabled(requireContext())
        preferenceProvider.putBooleanKey(App.SUGGESTION_PREF, isSystemOverlayEnabled(requireContext()))
    }

    private fun navigateToNextScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            navigateTo(Start.Screen.STANDARD_COPY)
        else
            navigateTo(Start.Screen.WINDOWS_APP)
    }
}
