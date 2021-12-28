package com.kpstv.xclipper.ui.fragments.sheets

import android.animation.Animator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.viewModels
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.getKeyArgs
import com.kpstv.navigation.hasKeyArgs
import com.kpstv.xclipper.core_addons.R
import com.kpstv.xclipper.core_addons.databinding.BottomSheetExtensionBinding
import com.kpstv.xclipper.data.model.ExtensionData
import com.kpstv.xclipper.data.model.ExtensionItem
import com.kpstv.xclipper.defaults.DefaultAnimator
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.ui.helpers.ExtensionHelper
import com.kpstv.xclipper.ui.viewmodel.ExtensionBottomSheetState
import com.kpstv.xclipper.ui.viewmodel.ExtensionBottomSheetViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class ExtensionBottomSheet : CustomRoundedBottomSheetFragment(R.layout.bottom_sheet_extension) {
    private val binding by viewBinding(BottomSheetExtensionBinding::bind)
    private val viewModel by viewModels<ExtensionBottomSheetViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasKeyArgs<Args>()) {
            val args = getKeyArgs<Args>()

            val states = arrayOf(
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(-android.R.attr.state_enabled),
            )
            val colors = intArrayOf(
                args.dominantColor,
                ColorUtils.blendARGB(args.dominantColor, Color.BLACK, 0.7f)
            )

            val colorTint = ColorStateList(states, colors)

            binding.tvTitle.text = args.title
            binding.tvDesc.text = args.fullDescription
            binding.ivLogo.setImageResource(args.icon)
            binding.ivLogo.imageTintList = colorTint
            binding.viewProgress.progressTintList = colorTint
            binding.btnActivate.backgroundTintList = colorTint
            binding.cardLogo.setCardBackgroundColor(ColorUtils.blendARGB(args.dominantColor, Color.BLACK, 0.5f))

            binding.btnActivate.setOnClickListener {
                viewModel.startPurchase(requireActivity(), args.sku)
            }

            observeActivation(args)

            viewModel.viewState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    is ExtensionBottomSheetState.Detail -> {
                        binding.viewDetail.show()
                        binding.viewProgress.hide()
                        binding.viewLottie.hide()
                    }
                    is ExtensionBottomSheetState.PurchaseStarted -> {
                        dialog?.setCancelable(false)
                        binding.viewDetail.hide()
                        binding.viewProgress.show()
                        binding.viewLottie.hide()
                    }
                    is ExtensionBottomSheetState.PurchaseCompleted -> {
                        binding.viewDetail.hide()
                        binding.viewProgress.hide()
                        binding.viewLottie.show()
                        binding.viewLottie.addAnimatorListener(object : Animator.AnimatorListener by DefaultAnimator {
                            override fun onAnimationEnd(animation: Animator?) {
                                dismiss()
                            }
                        })
                        binding.viewLottie.playAnimation()
                    }
                    is ExtensionBottomSheetState.PurchaseFailed -> {
                        dismiss()
                        val message: String = when(state.exception) {
                            is ExtensionHelper.BillingHelper.Exceptions.BillingClientNotReadyException -> getString(R.string.err_billing_not_ready)
                            is ExtensionHelper.BillingHelper.Exceptions.BillingServiceDisconnectedException -> getString(R.string.err_billing_disconnect)
                            is ExtensionHelper.BillingHelper.Exceptions.BillingSetupFinishedFailedException -> getString(R.string.err_billing_unfinished, state.exception.message)
                            is ExtensionHelper.BillingHelper.Exceptions.PurchaseNotAcknowledgedException -> getString(R.string.err_purchase_failed)
                            is ExtensionHelper.BillingHelper.Exceptions.PurchaseItemNotFoundException -> getString(R.string.err_purchase_no_item)
                            is ExtensionHelper.BillingHelper.Exceptions.PurchaseCancelledException -> { return@observe }
                            else -> getString(R.string.err_billing_unknown)
                        }
                        Toasty.error(requireContext(), message, Toasty.LENGTH_LONG).show()
                    }
                }
            }

        } else {
            throw IllegalArgumentException("No args found")
        }
    }

    private fun observeActivation(args: Args) {
        viewModel.observeActivationChange(args.sku).observe(viewLifecycleOwner) { unlocked ->
            if (unlocked) {
                binding.btnActivate.setText(R.string.subscribed)
                binding.btnActivate.setIconResource(R.drawable.addons_ic_check_circle)
                binding.btnActivate.setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    @Parcelize
    data class Args(
        override val title: String,
        override val fullDescription: String,
        override val icon: Int,
        override val dominantColor: Int,
        override val sku: String,
        override val smallDescription: String
    ) : ExtensionData, BaseArgs() {
        companion object {
            fun ExtensionItem.toExtensionBottomSheetArgs() = Args(
                title = title,
                fullDescription = fullDescription,
                icon = icon,
                dominantColor = dominantColor,
                sku = sku,
                smallDescription = smallDescription
            )
        }
    }
}