import StoreKit

@objc public enum DarwinProductType: Int {
    case consumable
    case nonConsumable
    case nonRenewable
    case autoRenewable
}

@objc public enum DarwinPurchaseResultType: Int {
    case success
    case pending
    case cancelled
    case failure
}

@objc public enum DarwinPurchaseEnvironment : Int {
    case production
    case sandbox
    case xcode
}

@objc public enum DarwinOwnershipType : Int {
    case familyShared
    case purchased
}

@objc public enum DarwinPurchaseReason : Int {
    case purchase
    case renewal
    case unknown
}

@objc public enum DarwinOfferType : Int {
    case code
    case promotional
    case introductory
    case none
}

@objc public enum DarwinPaymentMode : Int {
    case freeTrial
    case payAsYouGo
    case payUpFront
    case none
}

@objc public enum DarwinRevocationReason : Int {
    case developerIssue
    case other
    case none
}

@objcMembers public class DarwinTransaction: NSObject {
    public let environment: DarwinPurchaseEnvironment
    public let originalID: UInt64
    public let originalPurchaseDate: NSString
    public let id: UInt64
    public let webOrderLineItemID: NSString?
    public let appBundleID: NSString
    public let productID: NSString
    public let productType: DarwinProductType
    public let subscriptionGroupID: NSString?
    public let purchaseDate: NSString
    public let expirationDate: NSString?
    public let currencyCode: NSString?
    public let price: NSString?
    public let isUpgraded: Bool
    public let ownershipType: DarwinOwnershipType
    public let purchasedQuantity: Int
    public let purchaseReason: DarwinPurchaseReason
    public let offerID: NSString?
    public let offerType: DarwinOfferType
    public let offerPaymentMode: DarwinPaymentMode
    public let revocationDate: NSString?
    public let revocationReason: DarwinRevocationReason
    public let appAccountToken: NSString?
    public let verificationError: NSString?

    public init(
        environment: DarwinPurchaseEnvironment,
        originalID: UInt64,
        originalPurchaseDate: NSString,
        id: UInt64,
        webOrderLineItemID: NSString?,
        appBundleID: NSString,
        productID: NSString,
        productType: DarwinProductType,
        subscriptionGroupID: NSString?,
        purchaseDate: NSString,
        expirationDate: NSString?,
        currencyCode: NSString?,
        price: NSString?,
        isUpgraded: Bool,
        ownershipType: DarwinOwnershipType,
        purchasedQuantity: Int,
        purchaseReason: DarwinPurchaseReason,
        offerID: NSString?,
        offerType: DarwinOfferType,
        offerPaymentMode: DarwinPaymentMode,
        revocationDate: NSString?,
        revocationReason: DarwinRevocationReason,
        appAccountToken: NSString?,
        verificationError: NSString?
    ) {
        self.environment = environment
        self.originalID = originalID
        self.originalPurchaseDate = originalPurchaseDate
        self.id = id
        self.webOrderLineItemID = webOrderLineItemID
        self.appBundleID = appBundleID
        self.productID = productID
        self.productType = productType
        self.subscriptionGroupID = subscriptionGroupID
        self.purchaseDate = purchaseDate
        self.expirationDate = expirationDate
        self.currencyCode = currencyCode
        self.price = price
        self.isUpgraded = isUpgraded
        self.ownershipType = ownershipType
        self.purchasedQuantity = purchasedQuantity
        self.purchaseReason = purchaseReason
        self.offerID = offerID
        self.offerType = offerType
        self.offerPaymentMode = offerPaymentMode
        self.revocationDate = revocationDate
        self.revocationReason = revocationReason
        self.appAccountToken = appAccountToken
        self.verificationError = verificationError
    }
}

public class DarwinProduct : NSObject {
    @objc public let id: String
    @objc public let displayName: String
    @objc public let desc: String
    @objc public let price: String
    @objc public let displayPrice: String
    @objc public let currencyCode: String
    @objc public let type: DarwinProductType
    public let product: Product

    public init(
        id: String,
        displayName: String,
        desc: String,
        price: String,
        displayPrice: String,
        currencyCode: String,
        type: DarwinProductType,
        product: Product
    ) {
        self.id = id
        self.displayName = displayName
        self.desc = desc
        self.price = price
        self.displayPrice = displayPrice
        self.currencyCode = currencyCode
        self.type = type
        self.product = product
    }
}

@objcMembers public class DarwinProductResult : NSObject {
    public let products: [DarwinProduct]
    public let error: String?

    public init(
        products: [DarwinProduct],
        error: String?
    ) {
        self.products = products
        self.error = error
    }
}

@objcMembers public class DarwinTransactionsResult : NSObject {
    public let transactions: [DarwinTransaction]
    public let error: String?

    public init(
        transactions: [DarwinTransaction],
        error: String?
    ) {
        self.transactions = transactions
        self.error = error
    }
}

@objcMembers public class DarwinPurchaseResult : NSObject {
    public let type: DarwinPurchaseResultType
    public let transaction: DarwinTransaction?
    public let error: NSString?

    public init(
        type: DarwinPurchaseResultType,
        transaction: DarwinTransaction?,
        error: NSString?
    ) {
        self.type = type
        self.transaction = transaction
        self.error = error
    }
}

@objc public class StoreKitIntegration : NSObject {
    @objc public class func listenForTransactionUpdates() {
        Task {
            for await verificationResult in Transaction.updates {
                switch verificationResult {
                case .verified(let transaction):
                    await transaction.finish()
                    break
                case .unverified(let transaction, _):
                    await transaction.finish()
                    break
                }
            }
        }
    }

    @objc public class func getProducts(ids: [String], result: @escaping (DarwinProductResult) -> Void) {
        Task {
            do {
                let products = try await Product.products(for: ids)
                let darwinProduct = products.map {
                    let type = makeObjCCompatible(productType: $0.type)
                    return DarwinProduct(
                        id: $0.id,
                        displayName: $0.displayName,
                        desc: $0.description,
                        price: "\($0.price)",
                        displayPrice: $0.displayPrice,
                        currencyCode: $0.priceFormatStyle.currencyCode,
                        type: type,
                        product: $0
                    )
                }
                result(DarwinProductResult(products: darwinProduct, error: nil))
            } catch {
                result(DarwinProductResult(products: [], error: error.localizedDescription))
            }
        }
    }

    @objc public class func purchase(
        product: DarwinProduct,
        quantity: Int,
        userUUID: String?,
        result: @escaping (DarwinPurchaseResult) -> Void
    ) {
        let product = product.product
        let options: Set<Product.PurchaseOption>
        if let userUUID = userUUID, let uuid = UUID.init(uuidString: userUUID) {
            options = [
                Product.PurchaseOption.quantity(quantity),
                Product.PurchaseOption.appAccountToken(uuid)
            ]
        } else {
            options = [Product.PurchaseOption.quantity(quantity)]
        }
        Task {
            do {
                let purchaseResult = try await product.purchase(options: options)
                switch purchaseResult {
                case .success(let verificationResult):
                    switch verificationResult {
                    case .verified(let transaction):
                        result(
                            DarwinPurchaseResult(
                                type: .success,
                                transaction: makeObjCCompatible(transaction: transaction),
                                error: nil
                            )
                        )
                        break
                    case .unverified(let transaction, let verificationError):
                        let error = NSString(string: verificationError.localizedDescription)
                        result(
                            DarwinPurchaseResult(
                                type: .success,
                                transaction: makeObjCCompatible(
                                    transaction: transaction,
                                    verificationError: error
                                ),
                                error: error
                            )
                        )
                        break
                    }
                    break
                case .pending:
                    result(
                        DarwinPurchaseResult(
                            type: .pending,
                            transaction: nil,
                            error: nil
                        )
                    )
                    break
                case .userCancelled:
                    result(
                        DarwinPurchaseResult(
                            type: .cancelled,
                            transaction: nil,
                            error: nil
                        )
                    )
                    break
                @unknown default:
                    result(
                        DarwinPurchaseResult(
                            type: .failure,
                            transaction: nil,
                            error: nil
                        )
                    )
                    break
                }
            } catch {
                result(
                    DarwinPurchaseResult(
                        type: .failure,
                        transaction: nil,
                        error: NSString(string: error.localizedDescription)
                    )
                )
            }
        }
    }

    @objc public class func getTransactions(
        result: @escaping (DarwinTransactionsResult) -> Void
    ) {
        Task {
            var transactions: [DarwinTransaction] = []
            for await verificationResult in Transaction.currentEntitlements {
                switch verificationResult {
                case .verified(let transaction):
                    let darwinTransaction = makeObjCCompatible(transaction: transaction)
                    transactions.append(darwinTransaction)
                    break
                case .unverified(let transaction, let verificationError):
                    let error = NSString(string: verificationError.localizedDescription)
                    let darwinTransaction = makeObjCCompatible(
                        transaction: transaction,
                        verificationError: error
                    )
                    transactions.append(darwinTransaction)
                    break
                }
            }
            result(DarwinTransactionsResult(transactions: transactions, error: nil))
        }
    }

    private class func makeObjCCompatible(
        transaction: Transaction,
        verificationError: NSString? = nil
    ) -> DarwinTransaction {
        let env: DarwinPurchaseEnvironment
        if #available(iOS 16, macOS 13, *) {
            switch transaction.environment {
            case .production:
                env = DarwinPurchaseEnvironment.production
                break
            case .sandbox:
                env = DarwinPurchaseEnvironment.sandbox
                break
            case .xcode:
                env = DarwinPurchaseEnvironment.xcode
                break
            default:
                env = DarwinPurchaseEnvironment.production
                break
            }
        } else {
            switch transaction.environmentStringRepresentation {
            case "Sandbox":
                env = DarwinPurchaseEnvironment.sandbox
                break
            case "Xcode":
                env = DarwinPurchaseEnvironment.xcode
                break
            case "Production":
                env = DarwinPurchaseEnvironment.production
                break
            default:
                env = DarwinPurchaseEnvironment.production
                break
            }
        }

        let productType = makeObjCCompatible(productType: transaction.productType)

        let currencyCode: NSString?
        if #available(iOS 16, macOS 13.0, *) {
            currencyCode = transaction.currency?.identifier as NSString?
        } else {
            currencyCode = transaction.currencyCode as NSString?
        }

        let price: NSString?
        if let transactionPrice = transaction.price {
            price = NSString(string: "\(transactionPrice)")
        } else {
            price = nil
        }

        let ownershipType = switch transaction.ownershipType {
        case .familyShared:
            DarwinOwnershipType.familyShared
        case .purchased:
            DarwinOwnershipType.purchased
        default:
            DarwinOwnershipType.purchased
        }

        let reason: DarwinPurchaseReason
        if #available(iOS 17.0, macOS 14.0, *) {
            reason = switch transaction.reason {
            case .purchase:
                DarwinPurchaseReason.purchase
            case .renewal:
                DarwinPurchaseReason.renewal
            default:
                DarwinPurchaseReason.unknown
            }
        } else {
            reason = DarwinPurchaseReason.unknown
        }

        let offerID: NSString?
        let offerType: DarwinOfferType
        let offerPaymentMode: DarwinPaymentMode
        if #available(iOS 17.2, macOS 14.2, *) {
            offerID = transaction.offer?.id as NSString?
            offerType = makeObjCCompatible(offerType: transaction.offer?.type)
            offerPaymentMode = makeObjCCompatible(paymentMode: transaction.offer?.paymentMode)
        } else {
            offerID = transaction.offerID as NSString?
            offerType = makeObjCCompatible(offerType: transaction.offerType)
            if let paymentMode = transaction.offerPaymentModeStringRepresentation {
                switch paymentMode {
                    case "FreeTrial":
                        offerPaymentMode = DarwinPaymentMode.freeTrial
                        break
                    case "PayAsYouGo":
                        offerPaymentMode = DarwinPaymentMode.payAsYouGo
                        break
                    case "PayUpFront":
                        offerPaymentMode = DarwinPaymentMode.payUpFront
                        break
                    default:
                        offerPaymentMode = DarwinPaymentMode.none
                        break
                }
            } else {
                offerPaymentMode = DarwinPaymentMode.none
            }
        }


        let reasonRevoked: DarwinRevocationReason
        if let reason = transaction.revocationReason {
            switch reason {
            case .developerIssue:
                reasonRevoked = DarwinRevocationReason.developerIssue
                break
            case .other:
                reasonRevoked = DarwinRevocationReason.other
                break
            default:
                reasonRevoked = DarwinRevocationReason.none
                break
            }
        } else {
            reasonRevoked = DarwinRevocationReason.none
        }

        let appAccountToken: NSString?
        if let token = transaction.appAccountToken {
            appAccountToken = token.uuidString as NSString
        } else {
            appAccountToken = nil
        }

        let darwinTransaction = DarwinTransaction(
            environment: env,
            originalID: transaction.originalID,
            originalPurchaseDate: transaction.originalPurchaseDate.ISO8601Format() as NSString,
            id: transaction.id,
            webOrderLineItemID: transaction.webOrderLineItemID as NSString?,
            appBundleID: transaction.appBundleID as NSString,
            productID: transaction.productID as NSString,
            productType: productType,
            subscriptionGroupID: transaction.subscriptionGroupID as NSString?,
            purchaseDate: transaction.purchaseDate.ISO8601Format() as NSString,
            expirationDate: transaction.expirationDate?.ISO8601Format() as NSString?,
            currencyCode: currencyCode,
            price: price,
            isUpgraded: transaction.isUpgraded,
            ownershipType: ownershipType,
            purchasedQuantity: transaction.purchasedQuantity,
            purchaseReason: reason,
            offerID: offerID,
            offerType: offerType,
            offerPaymentMode: offerPaymentMode,
            revocationDate: transaction.revocationDate?.ISO8601Format() as NSString?,
            revocationReason: reasonRevoked,
            appAccountToken: appAccountToken,
            verificationError: verificationError
        )

        return darwinTransaction
    }

    private class func makeObjCCompatible(offerType: Transaction.OfferType?) -> DarwinOfferType {
        guard let type = offerType else { return DarwinOfferType.none }
        let darwinType = switch type {
        case .code:
            DarwinOfferType.code
        case .promotional:
            DarwinOfferType.promotional
        case .introductory:
            DarwinOfferType.introductory
        default:
            DarwinOfferType.none
        }
        return darwinType
    }

    @available(iOS 17.2, macOS 14.2, *)
    private class func makeObjCCompatible(paymentMode: Transaction.Offer.PaymentMode?) -> DarwinPaymentMode {
        guard let mode = paymentMode else { return DarwinPaymentMode.none }
        let darwinMode = switch mode {
        case .freeTrial:
            DarwinPaymentMode.freeTrial
        case .payAsYouGo:
            DarwinPaymentMode.payAsYouGo
        case .payUpFront:
            DarwinPaymentMode.payUpFront
        default:
            DarwinPaymentMode.payUpFront
        }
        return darwinMode
    }

    private class func makeObjCCompatible(productType: Product.ProductType) -> DarwinProductType {
        let type = switch productType {
        case .consumable:
            DarwinProductType.consumable
        case .autoRenewable:
            DarwinProductType.autoRenewable
        case .nonRenewable:
            DarwinProductType.nonRenewable
        case .nonConsumable:
            DarwinProductType.nonConsumable
        default:
            DarwinProductType.consumable
        }
        return type
    }
}
