package com.kpstv.xclipper.ui.fragments

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.FragmentUpgradeBinding
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.listeners.ResponseResult
import com.kpstv.xclipper.ui.helpers.extensions.AddOns
import com.kpstv.xclipper.ui.helpers.extensions.ExtensionAdapter
import com.kpstv.xclipper.ui.helpers.extensions.ExtensionAdapterItem.Companion.toAdapterItem
import com.kpstv.xclipper.ui.helpers.extensions.ExtensionBottomSheet
import com.kpstv.xclipper.ui.helpers.extensions.ExtensionBottomSheet.Args.Companion.toExtensionBottomSheetArgs
import com.kpstv.xclipper.ui.helpers.extensions.ExtensionHelper
import com.kpstv.xclipper.ui.viewmodels.UpgradeViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class Upgrades : AnimateFragment(R.layout.fragment_upgrade) {

    @Inject
    lateinit var firebaseProvider: FirebaseProvider
    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    private val viewModel by viewModels<UpgradeViewModel>()
    private val binding by viewBinding(FragmentUpgradeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.isEnabled = false

        binding.premiumCard.setButtonClickListener {
            launch(getString(R.string.app_website))
        }

        firebaseProvider.getLicenseStrategy().observe(viewLifecycleOwner) { licenseType ->
            when (licenseType) {
                LicenseType.Standard, LicenseType.Invalid -> {
                    loadLatestPriceFetch()
                }
                else -> {
                    binding.premiumFootnotes.collapse()
                    binding.premiumCard.setButtonText(getString(R.string.subscribed))
                    binding.premiumCard.setButtonColor(requireContext().colorFrom(R.color.activated))
                    binding.premiumCard.setButtonIcon(requireContext().drawableFrom(R.drawable.ic_check_circle))

                    binding.premiumCard.setButtonClickListener {
                        Toasty.info(requireContext(), getString(R.string.thank_support)).show()
                    }
                }
            }
        }

        updateExtensions()
    }

    private fun updateExtensions() {
        val items = AddOns.getAllExtensions(requireContext())
        val adapter = ExtensionAdapter(
            items = items.map { it.toAdapterItem() },
            viewLifecycleOwner = viewLifecycleOwner,
            provideExtensionHelper = { ExtensionHelper(requireContext(), preferenceProvider, it.sku) },
            itemClickListener = { position ->
                val args = items[position].toExtensionBottomSheetArgs()
                getSimpleNavigator().show(ExtensionBottomSheet::class, args)
            }
        )

        binding.rvExtension.adapter = adapter
    }

    private fun loadLatestPriceFetch() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            binding.root.isRefreshing = true
            viewModel.fetchLatestPrice(requireContext()).collect { result ->
                when(result) {
                    is ResponseResult.Complete ->   binding.premiumCard.setPurchaseAmount("$${result.data}")
                    is ResponseResult.Error -> Toasty.error(requireContext(), "Error: ${result.error.message}").show()
                }
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