package dev.henkle.stytch.utils

import co.touchlab.kermit.Logger
import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.model.sdk.InfoHeaderData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.awt.Desktop
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Modifier
import java.net.URI
import java.util.Locale

private const val DEFAULT_MODEL = "desktop"

@Suppress("ObjectPropertyName")
private var _platform: Platform? = null
private var userAgent: String? = null
private var osVersion: String? = null
private var mainClassPackageName: String? = null

internal actual val platform: Platform get() = _platform ?: getPlatform().also { _platform = it }

@JvmName(name = "getPlatformImpl")
private fun getPlatform(): Platform {
    val os = System.getProperty("os.name", "generic").lowercase(locale = Locale.ENGLISH)
    return when {
        "mac" in os || "darwin" in os -> Platform.macOSJvm
        "win" in os -> Platform.WindowsJvm
        "nux" in os -> Platform.LinuxJvm
        else -> Platform.UnknownJvm
    }
}

private fun getOSVersion(): String = osVersion ?: System.getProperty("os.version")
    .also { osVersion = it }

private fun getMainClassPackage(): String = mainClassPackageName ?: run {
    val pkgName = System.getProperty("sun.java.command")
        ?.takeIf { it.isNotEmpty() }
        ?.let { it.split(' ').firstOrNull()?.substringBeforeLast(delimiter = '.') }
        ?: Thread.getAllStackTraces().keys.firstOrNull { thread ->
            try {
                thread.stackTrace.lastOrNull()?.className?.let { className ->
                    if ('$' in className) className.substringBeforeLast('$') else className
                }?.let { className ->
                    val mainFunction = Class.forName(className)
                        .getDeclaredMethod("main", Array<String>::class.java)
                    Modifier.isStatic(mainFunction.modifiers)
                } == true
            } catch(ex: Exception) {
                false
            }
        }?.stackTrace?.lastOrNull()?.className?.substringBeforeLast(delimiter = '.')
        ?: ""
    pkgName.also { mainClassPackageName = it }
}

internal actual fun getInfoHeaderData(platform: PlatformStytchClient): InfoHeaderData {
    val screenSize = platform.currentScreenSizePx.value
        .let { (width, height) -> "($width, $height)" }

    return InfoHeaderData(
        appPackage = platform.appPackageName ?: getMainClassPackage(),
        appVersion = platform.appVersionString ?: UNKNOWN,
        osName = (_platform ?: getPlatform().also { _platform = it }).name,
        osVersion = getOSVersion(),
        deviceModel = platform.deviceModel ?: DEFAULT_MODEL,
        deviceScreenSize = screenSize,
    )
}

internal actual fun getUserAgent(): String = userAgent ?: run {
    val osVersion = getOSVersion()
    when (platform) {
        Platform.WindowsJvm -> {
            "Mozilla/5.0 (Windows NT $osVersion; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
        }
        Platform.macOSJvm -> {
            val version = osVersion.replace('.', '_')
            "Mozilla/5.0 (Macintosh; Intel Mac OS X $version) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
        }
        else -> {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
        }
    }
}.also { userAgent = it }

internal actual fun launchBrowser(
    url: String,
    defaultCallbackScheme: String,
    oauthTimeoutMin: UInt,
    platformStytchClient: PlatformStytchClient,
    callback: (uri: String) -> Unit,
) {
    launchBrowserAndResponseServer(
        callbackTimeoutMin = oauthTimeoutMin,
        callbackEndpointPath = platformStytchClient.oauthCallbackEndpoint,
        callbackEndpointPort = platformStytchClient.oauthCallbackPort,
        callbackRedirect = platformStytchClient.oauthCallbackRedirect,
        callback = callback,
        launchBrowser = {
            try {
                val desktopBrowserIsSupported = Desktop.isDesktopSupported() &&
                    Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
                if (desktopBrowserIsSupported) {
                    Desktop.getDesktop().browse(URI(url))
                } else {
                    throw Exception()
                }
            } catch (ex: Exception) {
                try {
                    openUrlInBrowserViaTerminal(url)
                } catch (ex: Exception) {
                    Logger.e("StytchKMP") { "Unable to launch a browser to start OAuth flow" }
                }
            }
        },
    )
}

@Throws(IOException::class)
private fun openUrlInBrowserViaTerminal(url: String) {
    val runtime: Runtime = Runtime.getRuntime()
    when (platform) {
        Platform.WindowsJvm -> runtime.exec("rundll32 url.dll,FileProtocolHandler $url")
        Platform.macOSJvm -> runtime.exec("open $url")
        else -> runtime.exec("xdg-open $url")
    }
}
