package dev.henkle.store.utils.ext

import platform.Foundation.NSData
import platform.Foundation.NSNumber

fun Int.asNSData(): NSData? = NSNumber(int = this).asNSData()
