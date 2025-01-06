package dev.henkle.payments.googlePlay

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import dev.henkle.context.ContextProvider
import dev.henkle.payments.model.PurchaseType

class GooglePlayBilling(
    enablePendingPurchases: Boolean = true,
) {
    private val listener = PurchasesUpdatedListener listener@{ billingResult, purchases ->
        purchases.get(1)
    }

    val client by lazy {
        BillingClient.newBuilder(ContextProvider.context!!)
            .run { if(enablePendingPurchases) enablePendingPurchases() else this }
            .setListener()
            .build()
    }

    init {
        ProductDetailsParams
            .newBuilder()
            .setOfferToken()
            .setProductDetails()
            .build()

        client.launchBillingFlow(
            Activity(),
            BillingFlowParams
                .newBuilder()
                .setIsOfferPersonalized()
                .setObfuscatedAccountId()
                .setObfuscatedProfileId()
                .setSubscriptionUpdateParams()
                .setProductDetailsParamsList()
                .build()
        )
    }


    private fun Purchase.toTransaction() {
        quantity
        orderId
        accountIdentifiers?.obfuscatedAccountId
        accountIdentifiers?.obfuscatedProfileId
        this.isAcknowledged
        this.isAutoRenewing
        this.purchaseState
        this.purchaseTime
        this.purchaseToken
        val products = this.products.map { it.toString() }

    }
}

