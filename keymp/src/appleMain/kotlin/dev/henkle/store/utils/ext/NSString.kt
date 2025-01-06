package dev.henkle.store.utils.ext

import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding

@Suppress("CAST_NEVER_SUCCEEDS")
fun NSString.asKString(): String = this as String

fun NSString.asNSData(): NSData? = dataUsingEncoding(encoding = NSUTF8StringEncoding)
