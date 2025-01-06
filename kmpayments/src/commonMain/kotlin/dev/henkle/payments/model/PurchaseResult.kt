package dev.henkle.payments.model

sealed interface PurchaseResult {
    data class Success(val transaction: Transaction) : PurchaseResult
    data object Pending : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Failure(val msg: String, val ex: Exception = Exception(msg)) : PurchaseResult
}