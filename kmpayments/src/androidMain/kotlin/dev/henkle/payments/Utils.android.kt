package dev.henkle.payments

import com.android.billingclient.api.ProductDetails
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.henkle.payments.model.AndroidProduct
import dev.henkle.payments.model.Product
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
): Product = AndroidProduct(
    id = id,
    displayName = displayName,
    description = description,
    price = price,
    displayPrice = displayPrice,
    currencyCode = currencyCode,
    type = type,
    playStoreProduct = nativePointer as ProductDetails,
)
