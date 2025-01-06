package dev.henkle.payments.storekit

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.henkle.payments.Store
import dev.henkle.payments.bd
import dev.henkle.payments.model.IOSProduct
import dev.henkle.payments.model.OfferType
import dev.henkle.payments.model.OwnershipType
import dev.henkle.payments.model.PaymentMode
import dev.henkle.payments.model.Product
import dev.henkle.payments.model.ProductResult
import dev.henkle.payments.model.PurchaseResult
import dev.henkle.payments.model.PurchaseType
import dev.henkle.payments.model.RevocationReason
import dev.henkle.payments.model.Transaction
import dev.henkle.payments.native.DarwinOfferType
import dev.henkle.payments.native.DarwinOfferTypeCode
import dev.henkle.payments.native.DarwinOfferTypeIntroductory
import dev.henkle.payments.native.DarwinOfferTypeNone
import dev.henkle.payments.native.DarwinOfferTypePromotional
import dev.henkle.payments.native.DarwinOwnershipType
import dev.henkle.payments.native.DarwinOwnershipTypeFamilyShared
import dev.henkle.payments.native.DarwinOwnershipTypePurchased
import dev.henkle.payments.native.DarwinPaymentMode
import dev.henkle.payments.native.DarwinPaymentModeFreeTrial
import dev.henkle.payments.native.DarwinPaymentModeNone
import dev.henkle.payments.native.DarwinPaymentModePayAsYouGo
import dev.henkle.payments.native.DarwinPaymentModePayUpFront
import dev.henkle.payments.native.DarwinProduct
import dev.henkle.payments.native.DarwinProductType
import dev.henkle.payments.native.DarwinProductTypeAutoRenewable
import dev.henkle.payments.native.DarwinProductTypeConsumable
import dev.henkle.payments.native.DarwinProductTypeNonConsumable
import dev.henkle.payments.native.DarwinProductTypeNonRenewable
import dev.henkle.payments.native.DarwinPurchaseResult
import dev.henkle.payments.native.DarwinPurchaseResultTypeCancelled
import dev.henkle.payments.native.DarwinPurchaseResultTypeFailure
import dev.henkle.payments.native.DarwinPurchaseResultTypePending
import dev.henkle.payments.native.DarwinPurchaseResultTypeSuccess
import dev.henkle.payments.native.DarwinRevocationReason
import dev.henkle.payments.native.DarwinRevocationReasonDeveloperIssue
import dev.henkle.payments.native.DarwinRevocationReasonNone
import dev.henkle.payments.native.DarwinRevocationReasonOther
import dev.henkle.payments.native.DarwinTransaction
import dev.henkle.payments.native.StoreKitIntegration
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
class StoreKitKt : Store {
    override suspend fun initialize() {
        StoreKitIntegration.listenForTransactionUpdates()
    }

    override suspend fun getProducts(
        ids: List<String>,
        bypassCache: Boolean,
    ): ProductResult = suspendCoroutine { continuation ->
        StoreKitIntegration.getProductsWithIds(ids = ids) { productResult ->
            if (productResult != null) {
                val error = productResult.error()
                if (error == null) {
                    @Suppress("UNCHECKED_CAST")
                    val skProducts = productResult.products() as List<DarwinProduct>
                    continuation.resume(
                        value = ProductResult.Success(products = skProducts.map(::toProduct)),
                    )
                } else {
                    continuation.resume(
                        value = ProductResult.Failure(ex = StoreKitException(msg = error)),
                    )
                }
            } else {
                continuation.resume(
                    value = ProductResult.Failure(
                        ex = StoreKitException(msg = "ProductResult was nil!"),
                    ),
                )
            }
        }
    }

    override suspend fun purchase(
        product: Product,
        quantity: UInt,
        userID: Uuid?,
        profileID: Uuid?,
    ): PurchaseResult {
        val iOSProduct = product as IOSProduct
        //TODO: if possible, use suspendCoroutine here instead of completable deferred
        // and remove the coroutines dependency
        val promise = CompletableDeferred<DarwinPurchaseResult?>()
        StoreKitIntegration.purchaseWithProduct(
            product = iOSProduct.storeKitProduct,
            quantity = quantity.toLong(),
            userUUID = userID?.toString(),
            result = promise::complete,
        )
        return promise.await()?.toPurchaseResult(products = listOf(product))
            ?: PurchaseResult.Failure(msg = "StoreKit purchase did not return a result!")
    }

    override suspend fun getPurchases() {
        TODO("Not yet implemented")
    }

    private fun toProduct(product: DarwinProduct): Product = IOSProduct(
        id = product.id(),
        displayName = product.displayName(),
        description = product.desc(),
        price = BigDecimal.parseString(product.price()),
        displayPrice = product.displayPrice(),
        currencyCode = product.currencyCode(),
        type = product.type().toProductType(),
        storeKitProduct = product,
    )

    @OptIn(ExperimentalNativeApi::class)
    private fun DarwinTransaction.toTransaction(products: List<Product>): Transaction {
        assert(products.size == 1) { "Every DarwinTransaction should only contain a single Product!" }
        return Transaction(
            id = id().toString(),
            lineItemID = webOrderLineItemID(),
            groupID = null,
            purchaseToken = null,
            products = products,
            type = productType().toProductType(),
            quantity = purchasedQuantity().toUInt(),
            timestamp = Instant.parse(purchaseDate()),
            price = try { price()?.bd ?: products.first().price } catch(_: Exception) { 0.bd },
            currency = currencyCode() ?: products.first().currencyCode,
            userID = appAccountToken()?.let { uuidFrom(string = it) },
            profileID = null,
            ownershipType = ownershipType().toOwnershipType(),
            isUpgraded = isUpgraded(),
            offerID = offerID(),
            offerType = offerType().toOfferType(),
            offerPaymentMode = offerPaymentMode().toPaymentMode(),
            revocationDate = revocationDate()?.let { dateString -> Instant.parse(dateString) },
            revocationReason = revocationReason().toRevocationReason(),
            expirationDate = expirationDate()?.let { dateString -> Instant.parse(dateString) },
            originalTransactionID = originalID().toString(),
            originalTransactionDate = Instant.parse(originalPurchaseDate()),
        )
    }

    private fun DarwinOwnershipType.toOwnershipType(): OwnershipType = when(this) {
        DarwinOwnershipTypeFamilyShared -> OwnershipType.FamilyShared
        DarwinOwnershipTypePurchased -> OwnershipType.Purchased
        else -> OwnershipType.Purchased
    }

    private fun DarwinOfferType.toOfferType(): OfferType? = when(this) {
        DarwinOfferTypeCode -> OfferType.Code
        DarwinOfferTypePromotional -> OfferType.Promotional
        DarwinOfferTypeIntroductory -> OfferType.Introductory
        DarwinOfferTypeNone -> null
        else -> null
    }

    private fun DarwinPaymentMode.toPaymentMode(): PaymentMode? = when(this) {
        DarwinPaymentModePayUpFront -> PaymentMode.PayUpFront
        DarwinPaymentModePayAsYouGo -> PaymentMode.PayAsYouGo
        DarwinPaymentModeFreeTrial -> PaymentMode.FreeTrial
        DarwinPaymentModeNone -> null
        else -> null
    }

    private fun DarwinRevocationReason.toRevocationReason(): RevocationReason? = when(this) {
        DarwinRevocationReasonDeveloperIssue -> RevocationReason.DeveloperIssue
        DarwinRevocationReasonOther -> RevocationReason.Other
        DarwinRevocationReasonNone -> null
        else -> null
    }

    private fun DarwinProductType.toProductType(): PurchaseType = when(this) {
        DarwinProductTypeConsumable -> PurchaseType.ConsumableOneTimePurchase
        DarwinProductTypeNonConsumable -> PurchaseType.LifetimeOneTimePurchase
        DarwinProductTypeNonRenewable -> PurchaseType.NonRenewableSubscription
        DarwinProductTypeAutoRenewable -> PurchaseType.AutoRenewableSubscription
        else -> PurchaseType.ConsumableOneTimePurchase
    }

    private fun DarwinPurchaseResult.toPurchaseResult(
        products: List<Product>,
    ): PurchaseResult = when(type()) {
        DarwinPurchaseResultTypeSuccess -> transaction()?.let {
            PurchaseResult.Success(transaction = it.toTransaction(products = products))
        } ?: PurchaseResult.Failure(msg = "StoreKit purchase was successful, but no DarwinTransaction was returned!")

        DarwinPurchaseResultTypeFailure ->
            PurchaseResult.Failure(msg = error() ?: "StoreKit purchase failed with an unknown error")

        DarwinPurchaseResultTypePending -> PurchaseResult.Pending

        DarwinPurchaseResultTypeCancelled -> PurchaseResult.Cancelled

        else -> PurchaseResult.Failure(msg = "StoreKit purchase failed with an unknown error")
    }
}
