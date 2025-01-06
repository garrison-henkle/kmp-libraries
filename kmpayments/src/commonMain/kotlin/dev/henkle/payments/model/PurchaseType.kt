package dev.henkle.payments.model

enum class PurchaseType {
    ConsumableOneTimePurchase,
    LifetimeOneTimePurchase,
    NonRenewableSubscription,
    AutoRenewableSubscription,
}
