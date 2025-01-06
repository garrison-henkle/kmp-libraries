package dev.henkle.payments

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.henkle.payments.model.Product
import dev.henkle.payments.model.PurchaseType

expect fun Product(
    id: String,
    displayName: String,
    description: String,
    price: BigDecimal,
    displayPrice: String,
    currencyCode: String,
    type: PurchaseType,
    nativePointer: Any,
): Product

val Byte.bd: BigDecimal get() = BigDecimal.fromByte(this)
val Short.bd: BigDecimal get() = BigDecimal.fromShort(this)
val Int.bd: BigDecimal get() = BigDecimal.fromInt(this)
val Long.bd: BigDecimal get() = BigDecimal.fromLong(this)
val Float.bd: BigDecimal get() = BigDecimal.fromFloat(this)
val Double.bd: BigDecimal get() = BigDecimal.fromDouble(this)
val String.bd: BigDecimal get() = BigDecimal.parseString(this)
