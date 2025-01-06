package dev.henkle.payments.model

sealed interface ProductResult {
    data class Success(val products: List<Product>) : ProductResult
    data class Failure(val ex: Exception) : ProductResult
}
