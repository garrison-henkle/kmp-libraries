package dev.henkle.payments.model

import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant

data class Transaction(
    val id: String,
    val lineItemID: String?,
    val groupID: String?,
    val purchaseToken: String?,
    val products: List<Product>,
    val type: PurchaseType,
    val quantity: UInt,
    val timestamp: Instant,
    val price: BigDecimal,
    val currency: String,
    val userID: Uuid?,
    val profileID: Uuid?,
    val ownershipType: OwnershipType,
    val isUpgraded: Boolean?,
    val offerID: String?,
    val offerType: OfferType?,
    val offerPaymentMode: PaymentMode?,
    val revocationDate: Instant?,
    val revocationReason: RevocationReason?,
    val expirationDate: Instant?,
    val originalTransactionID: String?,
    val originalTransactionDate: Instant?,
)
