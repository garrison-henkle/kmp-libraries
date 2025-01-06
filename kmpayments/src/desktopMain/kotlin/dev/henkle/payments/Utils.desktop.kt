package dev.henkle.payments

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.henkle.payments.model.Product
import dev.henkle.payments.model.ProductImpl
import dev.henkle.payments.model.PurchaseType

actual fun Product(
    id: String,
    displayName: String,
    description: String,
    price: BigDecimal,
    displayPrice: String,
    currencyCode: String,
    type: PurchaseType,
    nativePointer: Any,
): Product = ProductImpl(
    id = id,
    displayName = displayName,
    description = description,
    price = price,
    displayPrice = displayPrice,
    currencyCode = currencyCode,
    type = type,
)
