package dev.henkle.payments.model

import com.android.billingclient.api.ProductDetails
import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class AndroidProduct(
    override val id: String,
    override val displayName: String,
    override val description: String,
    override val price: BigDecimal,
    override val displayPrice: String,
    override val currencyCode: String,
    override val type: PurchaseType,
    val playStoreProduct: ProductDetails,
    override val nativePointer: Any = playStoreProduct,
) : Product
