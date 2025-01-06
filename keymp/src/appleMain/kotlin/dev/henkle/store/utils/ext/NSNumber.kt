package dev.henkle.store.utils.ext

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSKeyedArchiver
import platform.Foundation.NSNumber

@OptIn(ExperimentalForeignApi::class)
internal fun NSNumber.asNSData(): NSData? =
    NSKeyedArchiver.archivedDataWithRootObject(
        `object` = this,
        requiringSecureCoding = true,
        error = null,
    )
