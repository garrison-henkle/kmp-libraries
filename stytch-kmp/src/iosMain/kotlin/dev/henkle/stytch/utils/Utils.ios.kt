package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import platform.darwin.NSObject
import kotlin.math.roundToInt

internal actual val platform: Platform = Platform.iOS

@OptIn(ExperimentalForeignApi::class)
internal actual fun getPhysicalDeviceInfo(): PhysicalDeviceInfo = PhysicalDeviceInfo(
    screenSize = UIScreen.mainScreen.bounds
        .useContents { size.width.roundToInt() to size.height.roundToInt() },
    model = UIDevice.currentDevice.model.lowercase(),
    osName = UIDevice.currentDevice.systemName,
)

internal actual fun launchBrowser(
    url: String,
    defaultCallbackScheme: String,
    oauthTimeoutMin: UInt,
    platformStytchClient: PlatformStytchClient,
    callback: (uri: String) -> Unit,
) {
    NSURL.URLWithString(URLString = url)?.also { nsUrl ->
        ASWebAuthenticationSession(
            uRL = nsUrl,
            callbackURLScheme = platformStytchClient.overrideOAuthCallbackScheme
                ?: defaultCallbackScheme,
        ) { callbackNSUrl, _ ->
            callbackNSUrl?.absoluteString?.also { callback(it) }
        }.apply {
            presentationContextProvider = contextProvider
            start()
        }
    }
}

private val contextProvider by lazy {
    object : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
        override fun presentationAnchorForWebAuthenticationSession(
            session: ASWebAuthenticationSession,
        ): ASPresentationAnchor = UIApplication.sharedApplication.keyWindow
    }
}
