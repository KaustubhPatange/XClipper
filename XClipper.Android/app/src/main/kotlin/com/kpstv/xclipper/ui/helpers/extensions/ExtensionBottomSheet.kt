package com.kpstv.xclipper.ui.helpers.extensions

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
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.databinding.BottomSheetExtensionBinding
import com.kpstv.xclipper.extensions.DelegatedAnimator
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class ExtensionBottomSheet : CustomRoundedBottomSheetFragment(R.layout.bottom_sheet_extension) {
    private val binding by viewBinding(BottomSheetExtensionBinding::bind)
    private val viewModel by viewModels<ExtensionBottomSheetViewModel>()

    @Inject lateinit var preferenceProvider: PreferenceProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasKeyArgs<Args>()) {
            val args = getKeyArgs<Args>()

            binding.tvTitle.text = args.title
            binding.tvDesc.text = args.description
            binding.ivLogo.setImageResource(args.icon)
            binding.ivLogo.imageTintList = ColorStateList.valueOf(args.dominantColor)
            binding.btnActivate.backgroundTintList = ColorStateList.valueOf(args.dominantColor)
            binding.cardLogo.setCardBackgroundColor(ColorUtils.blendARGB(args.dominantColor, Color.BLACK, 0.4f))

            binding.btnActivate.setOnClickListener {
                viewModel.startPurchase(requireActivity(), args.sku)
            }

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
                        binding.viewLottie.addAnimatorListener(object : Animator.AnimatorListener by DelegatedAnimator {
                            override fun onAnimationEnd(animation: Animator?) {
                                dismiss()
                            }
                        })
                        binding.viewLottie.playAnimation()
                    }
                    is ExtensionBottomSheetState.PurchaseFailed -> {
                        val message: String = when(state.exception) {
                            is ExtensionHelper.BillingHelper.Exceptions.BillingClientNotReadyException -> getString(R.string.err_billing_not_ready)
                            is ExtensionHelper.BillingHelper.Exceptions.BillingServiceDisconnectedException -> getString(R.string.err_billing_disconnect)
                            is ExtensionHelper.BillingHelper.Exceptions.BillingSetupFinishedFailedException -> getString(R.string.err_billing_unfinished, state.exception.message)
                            is ExtensionHelper.BillingHelper.Exceptions.PurchaseNotAcknowledgedException -> getString(R.string.err_purchase_failed)
                            else -> getString(R.string.err_billing_unknown)
                        }
                        Toasty.error(requireContext(), message, Toasty.LENGTH_LONG).show()
                        // TODO: Should we add purchase failed animation
                        dismiss()
                    }
                }
            }

        } else {
            throw IllegalArgumentException("No args found")
        }
    }

    @Parcelize
    data class Args(
        override val title: String,
        override val description: String,
        override val icon: Int,
        override val dominantColor: Int,
        override val sku: String
    ) : ExtensionData, BaseArgs()
}