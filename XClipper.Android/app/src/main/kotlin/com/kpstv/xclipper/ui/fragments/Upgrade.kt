package com.kpstv.xclipper.ui.fragments

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.databinding.FragmentUpgradeBinding
import com.kpstv.xclipper.extensions.LicenseType
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.viewmodels.UpgradeViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class Upgrade : AnimateFragment(R.layout.fragment_upgrade) {

    @Inject
    lateinit var firebaseProvider: FirebaseProvider

    private val viewModel by viewModels<UpgradeViewModel>()
    private val binding by viewBinding(FragmentUpgradeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.isEnabled = false

        firebaseProvider.getLicenseStrategy().observe(viewLifecycleOwner) { licenseType ->
            when (licenseType) {
                LicenseType.Standard -> {
                    binding.supportLayout.hide()
                    loadLatestPriceFetch()
                }
                LicenseType.Invalid -> {
                    binding.supportLayout.hide()
                    loadLatestPriceFetch()
                }
                else -> {
                    binding.premiumCard.hide()
                    binding.supportLayout.show()

                    binding.supportLayoutText.startAnimation(
                        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                    )
                }
            }
        }

        binding.premiumCard.setButtonClickListener {
            launch(getString(R.string.app_website))
        }
    }

    private fun loadLatestPriceFetch() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            binding.root.isRefreshing = true
            viewModel.fetchLatestPrice(requireContext()).collect { result ->
                result.fold(
                    onSuccess = {
                        binding.premiumCard.setPurchaseAmount("$$it")
                    },
                    onFailure = {
                        Toasty.error(requireContext(), "Error: ${it.message}").show()
                    }
                )
                binding.root.isRefreshing = false
            }
        }
    }

    private fun launch(url: String) =
        startActivity(Intent(ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = FLAG_ACTIVITY_NEW_TASK
        })
}