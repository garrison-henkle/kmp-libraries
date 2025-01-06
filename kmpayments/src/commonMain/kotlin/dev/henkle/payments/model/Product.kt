package dev.henkle.payments.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal

interface Product {
    val id: String
    val displayName: String
    val description: String
    val price: BigDecimal
    val displayPrice: String
    val currencyCode: String
    val type: PurchaseType
    val nativePointer: Any
}
