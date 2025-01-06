package dev.henkle.store.keychain

import co.touchlab.kermit.Logger
import dev.henkle.store.utils.ext.asNSData
import dev.henkle.store.utils.ext.asUTF8
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.darwin.OSStatus

interface KeychainClient {
    @Throws(Error::class)
    fun get(item: KeychainItem): List<QueryResult>

    fun valueExistsForItem(item: KeychainItem): Boolean

    @Throws(Error::class)
    fun setValueForItem(item: KeychainItem, value: KeychainItem.Value)

    @Throws(Error::class)
    fun removeItem(item: KeychainItem)

    fun getString(item: KeychainItem): String? = try {
        get(item = item).firstOrNull()?.data?.asUTF8()
    } catch(ex: Error) {
        Logger.e("StoreKMP") { "KeychainClient getString error: $ex" }
        null
    }

    fun setString(item: KeychainItem, value: String): Unit = try {
        value.asNSData()?.also { nsData ->
            setValueForItem(
                item = item,
                value = KeychainItem.Value(
                    data = nsData,
                    account = null,
                    label = null,
                    generic = null,
                    accessPolicy = null,
                ),
            )
        }
        Unit
    } catch(ex: Error) {
        Logger.e("StoreKMP") { "KeychainClient setString error: $ex" }
    }

    data class QueryResult(
        val data: NSData,
        val createdAt: NSDate,
        val modifiedAt: NSDate,
        val label: String?,
        val account: String?,
        val generic: NSData?,
    )

    sealed class Error(message: String? = null) : Exception(message = message) {
        data object ResultMissingAccount : Error()
        data object ResultMissingDates : Error()
        data object ResultNotData: Error()
        data object UnableToCreateAccessControl : Error()
        data class UnhandledError(val status: OSStatus) : Error(message = "Unhandled error: $status")
    }
}
