package dev.henkle.store.utils.ext

import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding

@Suppress("CAST_NEVER_SUCCEEDS")
fun String.asNSString(): NSString = this as NSString

fun String.asNSData(): NSData? = asNSString().dataUsingEncoding(NSUTF8StringEncoding)
