package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSScreen
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL
import kotlin.math.roundToInt

internal actual val platform: Platform = Platform.macOS

@OptIn(ExperimentalForeignApi::class)
internal actual fun getPhysicalDeviceInfo(): PhysicalDeviceInfo = PhysicalDeviceInfo(
    screenSize = NSScreen.mainScreen?.frame
        ?.useContents { size.width.roundToInt() to size.height.roundToInt() } ?: (0 to 0),
    model = "macOS",
    osName = "macOS",
)

internal actual fun launchBrowser(
    url: String,
    defaultCallbackScheme: String,
    oauthTimeoutMin: UInt,
    platformStytchClient: PlatformStytchClient,
    callback: (uri: String) -> Unit,
) {
    NSURL.URLWithString(URLString = url)?.also { nsUrl ->
        launchBrowserAndResponseServer(
            callbackTimeoutMin = oauthTimeoutMin,
            callbackEndpointPath = platformStytchClient.oauthCallbackPath,
            callbackEndpointPort = platformStytchClient.oauthCallbackPort,
            callbackRedirect = platformStytchClient.oauthCallbackRedirect,
            callback = callback,
            launchBrowser = { NSWorkspace.sharedWorkspace.openURL(url = nsUrl) },
        )
    }
}
