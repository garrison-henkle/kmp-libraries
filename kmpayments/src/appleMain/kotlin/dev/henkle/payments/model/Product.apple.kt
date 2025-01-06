package dev.henkle.payments.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.henkle.payments.native.ProductInfo
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
data class IOSProduct(
    override val id: String,
    override val displayName: String,
    override val description: String,
    override val price: BigDecimal,
    override val displayPrice: String,
    override val currencyCode: String,
    override val type: PurchaseType,
    val storeKitProduct: ProductInfo,
    override val nativePointer: Any = storeKitProduct,
) : Product
