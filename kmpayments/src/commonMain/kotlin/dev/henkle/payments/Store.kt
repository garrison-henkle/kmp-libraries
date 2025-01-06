package dev.henkle.payments

import com.benasher44.uuid.Uuid
import dev.henkle.payments.model.Product
import dev.henkle.payments.model.ProductResult
import dev.henkle.payments.model.PurchaseResult

interface Store {
    suspend fun initialize()

    suspend fun getProducts(
        ids: List<String>,
        bypassCache: Boolean,
    ): ProductResult

    suspend fun purchase(
        product: Product,
        quantity: UInt = 1u,
        userID: Uuid? = null,
        profileID: Uuid? = null,
    ): PurchaseResult

    suspend fun getPurchases()
}
