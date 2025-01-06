package dev.henkle.store.keychain

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Security.SecAccessControlCreateFlags
import platform.Security.SecAccessControlCreateWithFlags
import platform.Security.SecAccessControlRef
import platform.Security.kSecAccessControlBiometryAny
import platform.Security.kSecAccessControlUserPresence
import platform.Security.kSecAttrAccessControl
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrGeneric
import platform.Security.kSecAttrLabel
import platform.Security.kSecAttrService
import platform.Security.kSecAttrSynchronizable
import platform.Security.kSecAttrSynchronizableAny
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitAll
import platform.Security.kSecReturnAttributes
import platform.Security.kSecReturnData
import platform.Security.kSecUseDataProtectionKeychain
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class)
class KeychainItem(name: String) {
    val baseQuery: Map<CFStringRef?, Any?> = mapOf(
        kSecClass to kSecClassGenericPassword,
        kSecAttrService to name,
        kSecUseDataProtectionKeychain to kCFBooleanTrue,
        kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlock,
    )

    val getQuery: Map<CFStringRef?, Any?> = baseQuery + mapOf(
        kSecReturnData to kCFBooleanTrue,
        kSecReturnAttributes to kCFBooleanTrue,
        kSecMatchLimit to kSecMatchLimitAll,
        kSecAttrSynchronizable to kSecAttrSynchronizableAny,
    )

    @Throws(KeychainClient.Error.UnableToCreateAccessControl::class)
    fun updateQuerySegment(value: Value): Map<CFStringRef?, Any?> {
        val query = mutableMapOf<CFStringRef?, Any?>(kSecValueData to value.data)

        value.account?.also { accountValue ->
            query[kSecAttrAccount] = accountValue
        }
        value.label?.also { labelValue ->
            query[kSecAttrLabel] = labelValue
        }
        value.generic?.also { genericValue ->
            query[kSecAttrGeneric] = genericValue
        }
        value.accessPolicy?.accessControl()?.also { accessControlValue ->
            query[kSecAttrAccessControl] = accessControlValue
        }

        return query
    }

    @Throws(KeychainClient.Error.UnableToCreateAccessControl::class)
    fun insertQuery(value: Value): Map<CFStringRef?, Any?> =
        baseQuery + updateQuerySegment(value = value)

    enum class AccessPolicy {
        DeviceOwnerAuthentication,
        DeviceOwnerAuthenticationWithBiometrics,
        ;

        @Throws(KeychainClient.Error.UnableToCreateAccessControl::class)
        fun accessControl(): SecAccessControlRef =
            memScoped {
                val error = alloc<CFErrorRefVar>()
                val flags: SecAccessControlCreateFlags = when (this@AccessPolicy) {
                    DeviceOwnerAuthentication -> kSecAccessControlUserPresence
                    DeviceOwnerAuthenticationWithBiometrics -> kSecAccessControlBiometryAny
                }
                SecAccessControlCreateWithFlags(
                    allocator = null,
                    protection = kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
                    flags = flags,
                    error = error.ptr,
                )
            } ?: throw KeychainClient.Error.UnableToCreateAccessControl
    }

    data class Value(
        val data: NSData,
        val account: String?,
        val label: String?,
        val generic: NSData?,
        val accessPolicy: AccessPolicy?,
    )
}
