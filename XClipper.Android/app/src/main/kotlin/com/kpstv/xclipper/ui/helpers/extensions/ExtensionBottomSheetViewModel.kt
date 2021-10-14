package com.kpstv.xclipper.ui.helpers.extensions

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.kpstv.xclipper.data.provider.PreferenceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ExtensionBottomSheetViewModel @Inject constructor(
    private val preferenceProvider: PreferenceProvider
): ViewModel() {
    private val extensionBottomSheetStateFlow = MutableStateFlow<ExtensionBottomSheetState>(ExtensionBottomSheetState.Detail)
    val viewState : LiveData<ExtensionBottomSheetState> = extensionBottomSheetStateFlow.asLiveData()

    fun startPurchase(activity: Activity, sku: String) {
        val billingHelper = ExtensionHelper.BillingHelper(activity, preferenceProvider, sku)
        billingHelper.registerErrorListener {
            billingHelper.dispose()
            extensionBottomSheetStateFlow.tryEmit(ExtensionBottomSheetState.PurchaseFailed(it))
        }
        billingHelper.registerOnPurchaseCompleteListener {
            extensionBottomSheetStateFlow.tryEmit(ExtensionBottomSheetState.PurchaseCompleted)
        }
        billingHelper.init { billingHelper.launch(activity) }
        extensionBottomSheetStateFlow.tryEmit(ExtensionBottomSheetState.PurchaseStarted)
    }

    fun observeActivationChange(sku: String) = ExtensionHelper.observePurchaseComplete(preferenceProvider, sku).asLiveData()
}

sealed interface ExtensionBottomSheetState {
    object Detail : ExtensionBottomSheetState
    object PurchaseStarted : ExtensionBottomSheetState
    object PurchaseCompleted : ExtensionBottomSheetState
    data class PurchaseFailed(val exception: Exception?) : ExtensionBottomSheetState
}