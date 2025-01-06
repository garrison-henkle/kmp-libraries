package dev.henkle.store.utils.ext

import platform.Foundation.NSData
import platform.Foundation.NSNumber

fun Long.asNSData(): NSData? = NSNumber(long = this).asNSData()
