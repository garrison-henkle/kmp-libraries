package dev.henkle.store.keychain

import co.touchlab.kermit.Logger
import dev.henkle.store.utils.ext.asKString
import dev.henkle.store.utils.ext.useCFDictionary
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFArrayGetCount
import platform.CoreFoundation.CFArrayGetValueAtIndex
import platform.CoreFoundation.CFArrayRefVar
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.LocalAuthentication.LAContext
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecInteractionNotAllowed
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrCreationDate
import platform.Security.kSecAttrGeneric
import platform.Security.kSecAttrLabel
import platform.Security.kSecAttrModificationDate
import platform.Security.kSecAttrSynchronizable
import platform.Security.kSecAttrSynchronizableAny
import platform.Security.kSecUseAuthenticationContext
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class)
class KeychainClientImpl : KeychainClient {
    /**
     * *SIDE EFFECT WARNING*
     *
     * @param query *SIDE EFFECT* - query that will be mutated
     */
    private fun updateQueryWithLAContext(query: MutableMap<CFStringRef?, Any?>): LAContext {
        val context = LAContext()
        context.localizedReason = AUTH_REASON
        query[kSecUseAuthenticationContext] = context
        return context
    }

    private fun exists(item: KeychainItem): Boolean = memScoped {
        val queryMap = item.getQuery.toMutableMap()
        val context = updateQueryWithLAContext(query = queryMap)
        context.interactionNotAllowed = true

        val results = alloc<CFTypeRefVar>()
        queryMap.useCFDictionary { query ->
            val status = SecItemCopyMatching(query = query, result = results.ptr)
            status == errSecSuccess || status == errSecInteractionNotAllowed
        }
    }

    @Throws(KeychainClient.Error::class)
    override fun get(item: KeychainItem): List<KeychainClient.QueryResult> = memScoped {
        val results = alloc<CFArrayRefVar>()

        val queryMap = item.getQuery.toMutableMap()
        updateQueryWithLAContext(query = queryMap)

        queryMap.useCFDictionary { query ->
            val status = SecItemCopyMatching(query = query, result = results.ptr.reinterpret())
            if (status == errSecSuccess) {
                val queryResults = mutableListOf<KeychainClient.QueryResult>()
                for (i in 0..<CFArrayGetCount(theArray = results.value)) {
                    val result: CFDictionaryRef? = CFArrayGetValueAtIndex(
                        theArray = results.value,
                        idx = i,
                    )?.reinterpret()

                    val data = result.get<NSData>(
                        key = kSecValueData,
                        error = KeychainClient.Error.ResultNotData,
                    )
                    val account = result.get<NSString>(
                        key = kSecAttrAccount,
                        error = KeychainClient.Error.ResultMissingAccount,
                    ).asKString()
                    val createdAt = result.get<NSDate>(
                        key = kSecAttrCreationDate,
                        error = KeychainClient.Error.ResultMissingDates,
                    )
                    val modifiedAt = result.get<NSDate>(
                        key = kSecAttrModificationDate,
                        error = KeychainClient.Error.ResultMissingDates,
                    )
                    val label = result.getOrNull<NSString>(key = kSecAttrLabel)?.asKString()
                    val generic = result.getOrNull<NSData>(key = kSecAttrGeneric)

                    queryResults += KeychainClient.QueryResult(
                        data = data,
                        account = account,
                        createdAt = createdAt,
                        modifiedAt = modifiedAt,
                        label = label,
                        generic = generic,
                    )
                }
                queryResults
            } else {
                emptyList()
            }
        }
    }

    override fun valueExistsForItem(item: KeychainItem): Boolean = exists(item = item)

    @Throws(KeychainClient.Error::class)
    override fun setValueForItem(item: KeychainItem, value: KeychainItem.Value) {
        val queryMap = item.baseQuery.toMutableMap()
        updateQueryWithLAContext(query = queryMap)

        val status = if (exists(item = item)) {
            queryMap.useCFDictionary { query ->
                item.updateQuerySegment(
                    value = value,
                ).useCFDictionary { attributes ->
                    SecItemUpdate(query = query, attributesToUpdate = attributes)
                }
            }
        } else {
            item.insertQuery(value = value).useCFDictionary { query ->
                SecItemAdd(attributes = query, result = null)
            }
        }
        if (status != errSecSuccess) {
            throw KeychainClient.Error.UnhandledError(status = status)
        }
    }

    @Throws(KeychainClient.Error::class)
    override fun removeItem(item: KeychainItem) {
        item.baseQuery.toMutableMap().apply {
            this[kSecAttrSynchronizable] = kSecAttrSynchronizableAny
        }.useCFDictionary { query ->
            val status = SecItemDelete(query = query)
            if (status != errSecSuccess && status != errSecItemNotFound) {
                throw KeychainClient.Error.UnhandledError(status = status)
            }
        }
    }


    @Throws(KeychainClient.Error::class)
    private inline fun <reified T: Any> CFDictionaryRef?.get(
        key: CFStringRef?,
        error: KeychainClient.Error,
    ): T {
        val dict = this
        val ref: CFTypeRef? = CFDictionaryGetValue(
            theDict = dict,
            key = key,
        )?.reinterpret()
        return ref?.let { CFBridgingRelease(X = it) as? T } ?: throw error
    }

    private inline fun <reified T: Any> CFDictionaryRef?.getOrNull(key: CFStringRef?): T? {
        val dict = this
        val ref: CFTypeRef? = CFDictionaryGetValue(
            theDict = dict,
            key = key,
        )?.reinterpret()
        return ref?.let {
            val converted = CFBridgingRelease(X = it) as? T
            if (converted == null) {
                Logger.w("StoreKMP") {
                    "Unexpected item type found for key '$key' in keychain"
                }
            }
            converted
        }
    }

    companion object {
        // TODO: add localization
        private const val AUTH_REASON = "Authenticate with biometrics"
    }
}
