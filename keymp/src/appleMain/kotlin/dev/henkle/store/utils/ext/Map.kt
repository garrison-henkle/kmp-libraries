package dev.henkle.store.utils.ext

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.LocalAuthentication.LAContext

@OptIn(ExperimentalForeignApi::class)
@Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
fun <K: CFStringRef?, V: Any?, T> Map<K, V>.useCFDictionary(
    block: (dictionary: CFDictionaryRef?) -> T,
): T {
    val map = this@useCFDictionary
    val retainedItems = mutableListOf<CFTypeRef?>()
    val dictionary = CFDictionaryCreateMutable(
        allocator = kCFAllocatorDefault,
        capacity = size.toLong(),
        keyCallBacks = null,
        valueCallBacks = null,
    )
    return try {
        fun Any?.addToDictAndRegisterForCleanup(key: CFStringRef?) {
            val retained = CFBridgingRetain(X = this)
            retainedItems += retained
            dictionary[key] = retained
        }

        for ((key, value) in map) {
            when (value) {
                is CFTypeRef -> dictionary[key] = value
                is String -> value.asNSData().addToDictAndRegisterForCleanup(key = key)
                is NSString -> value.asNSData().addToDictAndRegisterForCleanup(key = key)
                is Boolean -> {
                    val cfBool = if (value) kCFBooleanTrue else kCFBooleanFalse
                    dictionary[key] = cfBool
                }
                is Int -> value.asNSData().addToDictAndRegisterForCleanup(key = key)
                is Long -> value.asNSData().addToDictAndRegisterForCleanup(key = key)
                is LAContext,
                is NSMutableData,
                is NSData -> value.addToDictAndRegisterForCleanup(key = key)
                null -> dictionary[key] = null
                else -> {
                    Logger.w("StytchKMP") {
                        "An unsupported type was passed to CFDictionary and will be passed unaltered via CFBridgingRetain: $value type=<${value!!::class}>"
                    }
                    value.addToDictAndRegisterForCleanup(key = key)
                }
            }
        }

        block(dictionary)
    } finally {
        retainedItems.forEach { CFBridgingRelease(X = it) }
        CFBridgingRelease(X = dictionary)
    }
}

@OptIn(ExperimentalForeignApi::class)
private operator fun CFMutableDictionaryRef?.set(key: CFStringRef?, value: CFTypeRef?) {
    CFDictionaryAddValue(theDict = this@set, key = key, value = value)
}
