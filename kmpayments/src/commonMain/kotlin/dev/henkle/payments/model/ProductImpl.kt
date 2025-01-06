package dev.henkle.payments.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal

internal data class ProductImpl(
    override val id: String,
    override val displayName: String,
    override val description: String,
    override val price: BigDecimal,
    override val displayPrice: String,
    override val currencyCode: String,
    override val type: PurchaseType,
) : Product
