package com.kpstv.xclipper.ui.fragments

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.LicenseType
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_upgrade.*
import javax.inject.Inject

@AndroidEntryPoint
class Upgrade : Fragment(R.layout.fragment_upgrade) {

    @Inject lateinit var firebaseProvider: FirebaseProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseProvider.getLicenseStrategy().observe(viewLifecycleOwner, Observer { licenseType ->
            when (licenseType) {
                LicenseType.Standard -> {
                    supportLayout.hide()
                }
                LicenseType.Invalid -> {
                    supportLayout.hide()
                }
                else -> {
                    premiumCard.hide()
                    supportLayout.show()

                    supportLayout_text.startAnimation(
                        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                    )
                }
            }
        })

        premiumCard.setButtonClickListener {
            launch(getString(R.string.app_website))
        }
    }

    private fun launch(url: String) =
        startActivity(Intent(ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = FLAG_ACTIVITY_NEW_TASK
        })
}