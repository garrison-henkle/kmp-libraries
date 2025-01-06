package dev.henkle.store.utils.ext

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(BetaInteropApi::class)
fun NSData.asUTF8(): String? =
    NSString.create(data = this, encoding = NSUTF8StringEncoding)?.asKString()
