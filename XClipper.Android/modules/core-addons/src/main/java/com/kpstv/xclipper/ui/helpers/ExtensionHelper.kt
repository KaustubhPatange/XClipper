package com.kpstv.xclipper.ui.helpers

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.extensions.ErrorFunction
import com.kpstv.xclipper.extensions.SimpleFunction
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ExtensionHelper(private val context: Context, sku: String) {
    // TODO: Add work manager to also make sure if extensions are not expired.

    companion object {
        fun observePurchaseComplete(preferenceProvider: PreferenceProvider, sku: String) = preferenceProvider.observeBooleanKeyAsFlow(sku, false)
    }

    private val billingHelper = BillingHelper(context, sku)

    fun isActive() = billingHelper.isActive()

    fun observePurchaseComplete() = billingHelper.observeActiveness()

    internal class BillingHelper(context: Context, private val preferenceProvider: PreferenceProvider, private val sku: String) {
        constructor(context: Context, sku: String) : this(
            context = context,
            preferenceProvider = CommonReusableEntryPoints.get(context).preferenceProvider(),
            sku = sku
        )
        private var skuDetails: SkuDetails? = null
        private var errorListener: ErrorFunction? = null
        private var purchaseCompleteListener: SimpleFunction? = null

        private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                purchases.forEach { handlePurchase(it) }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                errorListener?.invoke(PurchaseCancelledException())
            }
        }

        private var billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        fun registerErrorListener(block: ErrorFunction) {
            errorListener = block
        }

        fun registerOnPurchaseCompleteListener(block: SimpleFunction) {
            purchaseCompleteListener = block
        }

        /**
         * Initialize before purchase
         */
        fun init(onComplete: () -> Unit = {}, onError: () -> Unit= {}) {
            val connection = object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                        querySkuDetails(onComplete = { skuDetails ->
                            this@BillingHelper.skuDetails = skuDetails
                            validatePurchase(onComplete = onComplete)
                        })
                    } else {
                        billingClient.endConnection()
                        errorListener?.invoke(BillingSetupFinishedFailedException(billingResult.debugMessage))
                        onError()
                    }
                }
                override fun onBillingServiceDisconnected() {
                    billingClient.endConnection()
                    errorListener?.invoke(BillingServiceDisconnectedException())
                }
            }
            billingClient.startConnection(connection)
        }

        suspend fun init() : Boolean = suspendCancellableCoroutine { continuation ->
            init(
                onComplete = { continuation.resume(true) },
                onError = { continuation.resume(false) }
            )
            continuation.invokeOnCancellation { billingClient.endConnection() }
        }

        /** Launch the billing flow for purchasing */
        fun launch(activity: Activity) {
            val sku = skuDetails ?: run {
                errorListener?.invoke(BillingClientNotReadyException())
                return
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(sku)
                .build()

            billingClient.launchBillingFlow(activity, flowParams).responseCode
        }

        /**
         * Is purchase active
         */
        fun isActive() = preferenceProvider.getBooleanKey(sku, false)

        /**
         * Observe as flow
         */
        fun observeActiveness() = preferenceProvider.observeBooleanKeyAsFlow(sku, false)

        fun dispose() {
            billingClient.endConnection()
        }

        private fun querySkuDetails(onComplete: (SkuDetails?) -> Unit = {}) {
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(listOf(sku)).setType(BillingClient.SkuType.INAPP)

            billingClient.querySkuDetailsAsync(params.build()) { _, skuDetailsList ->
                val product = skuDetailsList?.firstOrNull { it.sku == sku }
                if (product == null) {
                    errorListener?.invoke(PurchaseItemNotFoundException())
                    return@querySkuDetailsAsync
                } else {
                    onComplete.invoke(product)
                }
            }
        }

        private fun validatePurchase(onComplete: () -> Unit) {
            billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP) call@{ _, purchaseList ->
                if (purchaseList.isEmpty()) {
                    deactivatePurchase()
                } else {
                    val hasPurchased = purchaseList.any { record -> record.skus.contains(sku) }
                    if (hasPurchased) {
                        activatePurchase()
                    } else {
                        deactivatePurchase()
                    }
                }

                onComplete.invoke()
            }
        }

        private fun handlePurchase(purchase: Purchase) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) call@{ ackPurchaseResult ->
                        if (ackPurchaseResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            if (purchase.skus.contains(sku)) {
                                activatePurchase()
                                purchaseCompleteListener?.invoke()
                            }
                            return@call
                        } else {
                            errorListener?.invoke(PurchaseNotAcknowledgedException())
                        }
                    }
                }
            }
        }

        private fun activatePurchase() {
            preferenceProvider.putBooleanKey(sku, true)
        }

        private fun deactivatePurchase() {
            preferenceProvider.putBooleanKey(sku, false)
        }

        companion object Exceptions {
            class BillingSetupFinishedFailedException(message: String) : Exception(message)
            class BillingServiceDisconnectedException : Exception()
            class BillingClientNotReadyException : Exception()
            class PurchaseNotAcknowledgedException : Exception()
            class PurchaseItemNotFoundException : Exception()
            class PurchaseCancelledException : Exception()
        }
    }
}