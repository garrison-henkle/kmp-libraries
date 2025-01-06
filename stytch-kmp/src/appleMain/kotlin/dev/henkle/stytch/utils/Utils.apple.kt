package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.model.sdk.InfoHeaderData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo

private const val UNKNOWN_BUNDLE_ID = "unknown_bundle_id"

internal data class PhysicalDeviceInfo(
    val screenSize: Pair<Int, Int>,
    val model: String,
    val osName: String,
)

internal expect fun getPhysicalDeviceInfo(): PhysicalDeviceInfo

internal actual fun getInfoHeaderData(platform: PlatformStytchClient): InfoHeaderData {
    val info = getPhysicalDeviceInfo()
    val version = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String

    return InfoHeaderData(
        appPackage = NSBundle.mainBundle.bundleIdentifier ?: UNKNOWN_BUNDLE_ID,
        appVersion = version ?: "",
        osName = info.osName,
        osVersion = NSProcessInfo.processInfo.operatingSystemVersionString,
        deviceModel = info.model,
        deviceScreenSize = info.screenSize.let{ (width, height) -> "($width,$height)" },
    )
}

internal actual fun getUserAgent(): String {
    val name = NSBundle.mainBundle.bundleIdentifier
    val version = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
        ?: ""
    return "$name/$version CFNetwork/808.3 Darwin/16.3.0"
}

internal actual val IODispatcher: CoroutineDispatcher = Dispatchers.IO
