package com.kpstv.xclipper.ui.fragments.welcome

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import com.google.android.material.button.MaterialButton
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.FragmentImproveDetectionBinding
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.activities.Start

class ImproveDetection : AbstractWelcomeFragment() {
    override fun getConfigurations(): Configuration {
        val spannableString = SpannableStringBuilder()
        spannableString.append(getString(R.string.palette_improve_detection))
        spannableString.append("\n\n\n")

        val fromTextLength = spannableString.length

        spannableString.append(getString(R.string.adb_dialog_message2, requireContext().packageName), ForegroundColorSpan(requireContext().colorFrom(R.color.colorTextSecondaryLight)), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableString.setSpan(RelativeSizeSpan(0.9f), fromTextLength, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val binding = FragmentImproveDetectionBinding.inflate(layoutInflater)
        binding.btnLearnMore.setTextColor(requireContext().colorFrom(R.color.palette3))
        binding.btnLearnMore.setOnClickListener {
            Utils.commonUrlLaunch(requireContext(), getString(R.string.app_docs_improve_detect))
        }

        return Configuration(
            paletteId = R.color.palette_improve,
            nextPaletteId = R.color.palette3,
            textId = -1,
            text = spannableString,
            insertView = binding.root,
            nextTextId = R.string.next_4,
            directions = Start.Screen.ENABLE_SUGGESTIONS
        )
    }
}