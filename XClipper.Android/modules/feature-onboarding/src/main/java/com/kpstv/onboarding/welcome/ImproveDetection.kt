package com.kpstv.onboarding.welcome

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import com.kpstv.onboarding.internals.OnBoardingRoutes
import com.kpstv.welcome.R
import com.kpstv.welcome.databinding.FragmentImproveDetectionBinding
import com.kpstv.xclipper.extensions.colorFrom
import com.kpstv.xclipper.ui.utils.LaunchUtils

internal class ImproveDetection : AbstractWelcomeFragment() {
    override fun getConfigurations(): Configuration {
        val spannableString = SpannableStringBuilder()
        spannableString.append(getString(R.string.palette_improve_detection))
        spannableString.append("\n\n\n")

        val fromTextLength = spannableString.length

        spannableString.append(getString(R.string.adb_detection_instruction, requireContext().packageName), ForegroundColorSpan(requireContext().colorFrom(R.color.colorTextSecondaryLight)), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableString.setSpan(RelativeSizeSpan(0.9f), fromTextLength, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val binding = FragmentImproveDetectionBinding.inflate(layoutInflater)
        binding.btnLearnMore.setTextColor(requireContext().colorFrom(R.color.palette3))
        binding.btnLearnMore.setOnClickListener {
            LaunchUtils.commonUrlLaunch(requireContext(), getString(R.string.app_docs_improve_detect))
        }

        return Configuration(
            paletteId = R.color.palette_improve,
            nextPaletteId = R.color.palette3,
            textId = -1,
            text = spannableString,
            insertView = binding.root,
            nextTextId = R.string.next_4,
            directions = OnBoardingRoutes.ENABLE_SUGGESTIONS
        )
    }
}